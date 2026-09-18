package ru.leonaugust.link_aggregator.kafka.link.categorize.producer;

import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkCategorizeEventProducer;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSaveEventProducer;
import ru.leonaugust.link_aggregator.domain.link.LinkCategorizeEvent;
import ru.leonaugust.link_aggregator.domain.link.LinkSaveEvent;
import ru.leonaugust.link_aggregator.kafka.common.AbstractKafkaProducer;
import ru.leonaugust.link_aggregator.kafka.common.KafkaTopicConfig;

@Component
@Slf4j
public class LinkCategorizeEventProducerKafkaImpl extends AbstractKafkaProducer<LinkCategorizeEvent> implements LinkCategorizeEventProducer {

  protected LinkCategorizeEventProducerKafkaImpl(KafkaTemplate<String, LinkCategorizeEvent> kafkaTemplate) {
    super(kafkaTemplate);
  }

  @Override
  public void send(LinkCategorizeEvent event) {
    super.send(event);
  }

  @Override
  protected String getTopicName() {
    return KafkaTopicConfig.CATEGORIZE;
  }
}
