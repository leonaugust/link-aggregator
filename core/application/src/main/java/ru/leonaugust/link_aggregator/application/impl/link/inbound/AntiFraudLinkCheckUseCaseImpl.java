package ru.leonaugust.link_aggregator.application.impl.link.inbound;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.leonaugust.link_aggregator.application.UseCase;
import ru.leonaugust.link_aggregator.application.api.link.inbound.AntiFraudLinkCheckUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSafetyCheckPort;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkSafetyViolationException;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class AntiFraudLinkCheckUseCaseImpl implements AntiFraudLinkCheckUseCase {

  private final LinkSafetyCheckPort linkSafetyCheckPort;

  @Override
  public void check(Link link) {
    if (link == null || link.url() == null || link.url().isBlank()) {
      throw new IllegalArgumentException("Ссылка не может быть пустой");
    }
    var url = validateUrl(link.url());

    var verdict = linkSafetyCheckPort.check(url);
    log.info("Результат антифрод-проверки ссылки {}: suspicious={}, source={}, detail={}",
        url, verdict.suspicious(), verdict.source(), verdict.detail());

    if (verdict.suspicious()) {
      throw new LinkSafetyViolationException(url, verdict.source(), verdict.detail());
    }
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