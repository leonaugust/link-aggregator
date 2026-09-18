package ru.leonaugust.link_aggregator.kafka.link.save.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSaveEventProducer;
import ru.leonaugust.link_aggregator.domain.link.LinkSaveEvent;
import ru.leonaugust.link_aggregator.kafka.common.AbstractKafkaProducer;
import ru.leonaugust.link_aggregator.kafka.common.KafkaTopicConfig;

@Component
@Slf4j
public class LinkSaveEventProducerKafkaImpl extends AbstractKafkaProducer<LinkSaveEvent> implements LinkSaveEventProducer {

  protected LinkSaveEventProducerKafkaImpl(KafkaTemplate<String, LinkSaveEvent> kafkaTemplate) {
    super(kafkaTemplate);
  }

  @Override
    public void send(LinkSaveEvent event) {
        super.send(event);
    }

  @Override
  protected String getTopicName() {
    return KafkaTopicConfig.SAVE;
  }
}
