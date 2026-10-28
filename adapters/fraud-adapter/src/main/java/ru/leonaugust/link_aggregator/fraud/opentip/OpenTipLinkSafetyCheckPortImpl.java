package ru.leonaugust.link_aggregator.fraud.opentip;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSafetyCheckPort;
import ru.leonaugust.link_aggregator.domain.link.LinkSafetyVerdict;
import ru.leonaugust.link_aggregator.fraud.config.FraudCheckProperties;

/**
 * Проверка ссылок через Kaspersky OpenTIP:
 * {@code GET {base-url}/search/{url|domain}?request={значение}}, заголовок {@code x-api-key}.
 *
 * <p>Вердикт определяется единственным полем {@code Zone} ответа. Пустой ответ, отсутствие данных
 * (404) и ошибки транспорта обрабатываются политикой {@code fail-open}, чтобы недоступность
 * внешнего сервиса не приводила к отказу в сохранении ссылок.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "link-aggregator.fraud-check.enabled", havingValue = "true")
@ConditionalOnProperty(name = "link-aggregator.fraud-check.provider", havingValue = "opentip")
public class OpenTipLinkSafetyCheckPortImpl implements LinkSafetyCheckPort {

  private static final String SOURCE = "opentip";
  private static final String URL_LOOKUP_PATH = "/search/url";
  private static final String DOMAIN_LOOKUP_PATH = "/search/domain";
  private static final String REQUEST_PARAM = "request";
  private static final String API_KEY_HEADER = "x-api-key";
  private static final int MAX_WEB_ADDRESS_LENGTH = 2000;
  private static final int HTTP_UNAUTHORIZED = 401;
  private static final int HTTP_FORBIDDEN = 403;
  private static final int HTTP_NOT_FOUND = 404;

  private final WebClient openTipWebClient;
  private final FraudCheckProperties properties;

  public OpenTipLinkSafetyCheckPortImpl(@Qualifier("openTipWebClient") WebClient openTipWebClient,
      FraudCheckProperties properties) {
    this.openTipWebClient = openTipWebClient;
    this.properties = properties;
  }

  @Override
  public LinkSafetyVerdict check(String url) {
    LookupRequest lookup;
    try {
      lookup = buildLookup(url);
    } catch (LookupNotPossibleException e) {
      return failPolicy(url, e.getMessage(), null);
    }
    OpenTipLookupResponse response;
    try {
      var requestSpec = openTipWebClient.get()
          .uri(builder -> builder.path(lookup.path())
              .queryParam(REQUEST_PARAM, lookup.value())
              .build());
      if (properties.apiKey() != null && !properties.apiKey().isBlank()) {
        requestSpec.header(API_KEY_HEADER, properties.apiKey());
      }
      response = requestSpec
          .retrieve()
          .bodyToMono(OpenTipLookupResponse.class)
          .block(properties.requestTimeout());
    } catch (WebClientResponseException e) {
      return handleStatus(url, e.getStatusCode().value(), e);
    } catch (RuntimeException e) {
      // catch намеренно широкий: охватывает всё, что нужно для получения и чтения ответа.
      // Исключение разбора тела не должно улетать в консюмер, который перезапустит событие по кругу.
      return failPolicy(url, "транспортная ошибка или таймаут: " + e.getMessage(), e);
    }
    return map(url, response);
  }

  /**
   * Выбирает вид поиска OpenTIP. Адрес без пути идёт в поиск по домену: поиск по URL требует
   * полный веб-адрес с путём и отвечает 400 на голый хост.
   *
   * @throws LookupNotPossibleException если проверка в принципе невозможна
   */
  private LookupRequest buildLookup(String url) {
    if (url == null || url.isBlank()) {
      throw new LookupNotPossibleException("пустая ссылка");
    }
    if (url.length() > MAX_WEB_ADDRESS_LENGTH) {
      throw new LookupNotPossibleException("адрес длиннее %d символов (%d символов), OpenTIP вернул бы 414"
          .formatted(MAX_WEB_ADDRESS_LENGTH, url.length()));
    }
    var uri = parse(url);
    if (uri == null) {
      throw new LookupNotPossibleException("не удалось разобрать ссылку как URI: " + url);
    }
    var host = uri.getHost();
    if (host == null || host.isBlank()) {
      throw new LookupNotPossibleException("не удалось определить домен в ссылке: " + url);
    }
    var path = uri.getPath();
    if (path == null || path.isBlank() || "/".equals(path)) {
      return new LookupRequest(DOMAIN_LOOKUP_PATH, host);
    }
    return new LookupRequest(URL_LOOKUP_PATH, url);
  }

  private URI parse(String url) {
    try {
      return new URI(url);
    } catch (URISyntaxException e) {
      return null;
    }
  }

  private LinkSafetyVerdict map(String url, OpenTipLookupResponse response) {
    if (response == null || response.zone() == null || response.zone().isBlank()) {
      log.warn("OpenTIP не вернул зону для ссылки {} (пустой или неполный ответ), считаем безопасной", url);
      return LinkSafetyVerdict.safe(SOURCE);
    }
    var zone = OpenTipZone.from(response.zone());
    if (zone.blocking()) {
      return LinkSafetyVerdict.violated(SOURCE, "zone=" + zone.name());
    }
    if (zone == OpenTipZone.YELLOW) {
      log.warn("OpenTIP вернул зону Yellow для ссылки {} — подозрительный контент без вредоносности, "
          + "проверка не блокирующая", url);
    }
    if (zone == OpenTipZone.UNKNOWN) {
      log.warn("OpenTIP вернул неизвестную зону '{}' для ссылки {} — вероятно, контракт API изменился, "
          + "считаем безопасной", response.zone(), url);
    }
    return LinkSafetyVerdict.safe(SOURCE);
  }

  private LinkSafetyVerdict handleStatus(String url, int status, RuntimeException cause) {
    if (status == HTTP_NOT_FOUND) {
      log.debug("OpenTIP не нашёл данных по ссылке {} (404), это не признак вредоносности", url);
      return LinkSafetyVerdict.safe(SOURCE);
    }
    if (status == HTTP_UNAUTHORIZED) {
      log.error("OpenTIP отклонил запрос: недействительный или просроченный API-токен (401). "
          + "Проверьте link-aggregator.fraud-check.api-key или переменную окружения OPENTIP_API_KEY");
      return failPolicy(url, "недействительный или просроченный API-токен (401)", cause);
    }
    if (status == HTTP_FORBIDDEN) {
      log.warn("Исчерпана квота запросов OpenTIP (403) при проверке ссылки {}", url);
      return failPolicy(url, "исчерпана квота запросов OpenTIP (403)", cause);
    }
    log.debug("OpenTIP ответил статусом {} на запрос проверки ссылки {}", status, url);
    return failPolicy(url, "ответ OpenTIP со статусом " + status, cause);
  }

  private LinkSafetyVerdict failPolicy(String url, String reason, RuntimeException cause) {
    if (properties.failOpen()) {
      log.warn("Антифрод-проверка ссылки {} через OpenTIP невозможна: {} (fail-open), считаем безопасной",
          url, reason);
      return LinkSafetyVerdict.safe(SOURCE);
    }
    if (cause != null) {
      throw cause;
    }
    throw new IllegalStateException("Антифрод-проверка ссылки " + url + " невозможна: " + reason);
  }

  private record LookupRequest(String path, String value) {
  }

  /**
   * Служебный сигнал: ссылка непригодна для проверки, запрос к OpenTIP не отправлялся.
   * Причина попадает в сообщение, чтобы политика {@code fail-open} залогировала её один раз.
   */
  private static class LookupNotPossibleException extends RuntimeException {

    LookupNotPossibleException(String message) {
      super(message);
    }
  }
}