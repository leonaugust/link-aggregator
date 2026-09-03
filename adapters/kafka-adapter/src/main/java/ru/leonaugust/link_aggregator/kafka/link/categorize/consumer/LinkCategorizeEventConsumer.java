package ru.leonaugust.link_aggregator.kafka.link.categorize.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.link.inbound.CategorizeLinkUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkRepository;
import ru.leonaugust.link_aggregator.domain.link.LinkCategorizeEvent;
import ru.leonaugust.link_aggregator.kafka.common.KafkaTopicConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkCategorizeEventConsumer {

  private final LinkRepository linkRepository;
  private final CategorizeLinkUseCase categorizeLinkUseCase;

  @KafkaListener(topics = KafkaTopicConfig.CATEGORIZE, groupId = "link-processor-group")
  public void consume(LinkCategorizeEvent event) {
    log.info("Received event: {}", event);
    var link = event.getLink();
    link = link.withCategory(categorizeLinkUseCase.categorize(link.url()).block());
    linkRepository.update(link.id(), link);
  }
}