package ru.leonaugust.link_aggregator.kafka.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;

@ExtendWith(MockitoExtension.class)
class KafkaErrorHandlingConfigUTest {

  @Mock
  private KafkaTemplate<Object, Object> kafkaTemplate;

  @Test
  void kafkaCommonErrorHandler_configured_returnsDefaultErrorHandler() {
    var config = new KafkaErrorHandlingConfig(kafkaTemplate, 1000L, 2L);

    assertThat(config.kafkaCommonErrorHandler(config.deadLetterPublishingRecoverer()))
        .isInstanceOf(DefaultErrorHandler.class);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void deadLetterPublishingRecoverer_failedRecord_publishesToDlt() {
    when(kafkaTemplate.send(any(ProducerRecord.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
    var config = new KafkaErrorHandlingConfig(kafkaTemplate, 1000L, 2L);
    var record = new ConsumerRecord<>("link-verify", 0, 5L, "key", "value");

    config.deadLetterPublishingRecoverer().accept(record, new RuntimeException("boom"));

    var captor = ArgumentCaptor.forClass(ProducerRecord.class);
    verify(kafkaTemplate).send(captor.capture());
    assertThat(captor.getValue().topic()).isEqualTo("link-verify.DLT");
  }
}
