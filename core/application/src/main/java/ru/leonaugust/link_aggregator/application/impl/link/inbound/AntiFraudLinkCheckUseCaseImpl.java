package ru.leonaugust.link_aggregator.application.impl.link.inbound;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.leonaugust.link_aggregator.application.UseCase;
import ru.leonaugust.link_aggregator.application.api.link.inbound.AntiFraudLinkCheckUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSafetyCheckPort;
import ru.leonaugust.link_aggregator.application.api.link.outbound.PromptInjectionDetector;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkVerification;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class AntiFraudLinkCheckUseCaseImpl implements AntiFraudLinkCheckUseCase {

  private final LinkSafetyCheckPort linkSafetyCheckPort;
  private final ContentFetcherPort contentFetcherPort;
  private final PromptInjectionDetector promptInjectionDetector;

  @Override
  public LinkVerification check(Link link) {
    if (link == null || link.url() == null || link.url().isBlank()) {
      throw new IllegalArgumentException("Ссылка не может быть пустой");
    }
    var url = validateUrl(link.url());

    var verdict = linkSafetyCheckPort.check(url);
    log.info("Результат антифрод-проверки ссылки {}: suspicious={}, source={}, detail={}",
        url, verdict.suspicious(), verdict.source(), verdict.detail());
    if (verdict.suspicious()) {
      return LinkVerification.suspicious();
    }

    var contentPage = contentFetcherPort.fetch(url);
    var isInjection = promptInjectionDetector.isInjection(contentPage.text()).block();
    if (Boolean.TRUE.equals(isInjection)) {
      log.warn("В контенте ссылки {} обнаружена попытка prompt injection, ссылка помечена как подозрительная", url);
      return LinkVerification.suspicious(contentPage);
    }
    return LinkVerification.ok(contentPage);
  }

  private String validateUrl(String url) {
    try {
      new URI(url);
      return url;
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Некорректный URL: " + url, e);
    }
  }
}
