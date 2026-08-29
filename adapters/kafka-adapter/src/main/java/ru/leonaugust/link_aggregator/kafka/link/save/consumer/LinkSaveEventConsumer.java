package ru.leonaugust.link_aggregator.kafka.link.save.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkCategorizeEventProducer;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkRepository;
import ru.leonaugust.link_aggregator.domain.link.LinkCategorizeEvent;
import ru.leonaugust.link_aggregator.domain.link.LinkSaveEvent;
import ru.leonaugust.link_aggregator.kafka.common.KafkaTopicConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkSaveEventConsumer {

  private final LinkRepository linkRepository;
  private final LinkCategorizeEventProducer linkSaveEventProducer;

  @KafkaListener(topics = KafkaTopicConfig.SAVE, groupId = "link-processor-group")
  public void consume(LinkSaveEvent event) {
    log.info("Received event: {}", event);
    var link = linkRepository.save(event.getLink(), event.getUserSearchCriteria());
    linkSaveEventProducer.send(LinkCategorizeEvent.builder()
        .key(link.id().toString())
        .link(link)
        .build());
  }
}