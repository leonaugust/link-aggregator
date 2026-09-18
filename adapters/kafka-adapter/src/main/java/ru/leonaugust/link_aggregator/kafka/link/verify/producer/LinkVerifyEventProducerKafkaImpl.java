package ru.leonaugust.link_aggregator.kafka.link.verify.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkVerifyEventProducer;
import ru.leonaugust.link_aggregator.domain.link.LinkVerifyEvent;
import ru.leonaugust.link_aggregator.kafka.common.AbstractKafkaProducer;
import ru.leonaugust.link_aggregator.kafka.common.KafkaTopicConfig;

@Component
@Slf4j
public class LinkVerifyEventProducerKafkaImpl extends AbstractKafkaProducer<LinkVerifyEvent> implements LinkVerifyEventProducer {

  protected LinkVerifyEventProducerKafkaImpl(KafkaTemplate<String, LinkVerifyEvent> kafkaTemplate) {
    super(kafkaTemplate);
  }

  @Override
    public void send(LinkVerifyEvent event) {
        super.send(event);
    }

  @Override
  protected String getTopicName() {
    return KafkaTopicConfig.VERIFY;
  }
}
