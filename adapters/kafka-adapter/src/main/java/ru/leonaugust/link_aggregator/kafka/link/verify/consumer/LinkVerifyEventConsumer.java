package ru.leonaugust.link_aggregator.kafka.link.verify.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.link.inbound.AntiFraudLinkCheckUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSaveEventProducer;
import ru.leonaugust.link_aggregator.domain.link.LinkSafetyViolationException;
import ru.leonaugust.link_aggregator.domain.link.LinkSaveEvent;
import ru.leonaugust.link_aggregator.domain.link.LinkVerifyEvent;
import ru.leonaugust.link_aggregator.kafka.common.KafkaTopicConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkVerifyEventConsumer {

  private final AntiFraudLinkCheckUseCase antiFraudLinkCheckUseCase;
  private final LinkSaveEventProducer linkSaveEventProducer;

  @KafkaListener(topics = KafkaTopicConfig.VERIFY, groupId = "link-processor-group")
  public void consume(LinkVerifyEvent event) {
    log.info("Received event: {}", event);
    try {
      antiFraudLinkCheckUseCase.check(event.getLink());
    } catch (LinkSafetyViolationException e) {
      log.warn("Link blocked by fraud check, save skipped: {}", e.getMessage());
      return;
    }
    linkSaveEventProducer.send(LinkSaveEvent.builder()
        .key(event.getKey())
        .link(event.getLink())
        .userSearchCriteria(event.getUserSearchCriteria())
        .build());
  }
}