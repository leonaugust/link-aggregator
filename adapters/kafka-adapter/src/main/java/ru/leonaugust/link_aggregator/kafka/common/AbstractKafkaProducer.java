package ru.leonaugust.link_aggregator.kafka.common;

import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import ru.leonaugust.link_aggregator.domain.AbstractEvent;

@Slf4j
public abstract class AbstractKafkaProducer<T extends AbstractEvent> {

  private final KafkaTemplate<String, T> kafkaTemplate;

  protected AbstractKafkaProducer(KafkaTemplate<String, T> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send(T event) {
    kafkaTemplate.send(getTopicName(), event.getKey(), event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("Failed to publish event: {}", event, ex);
          } else {
            log.debug("Published event: {}", event);
          }
        });
  }

  protected abstract String getTopicName();

}
