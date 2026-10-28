package ru.leonaugust.link_aggregator.kafka.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

class KafkaDltDestinationResolverUTest {

  private final KafkaDltDestinationResolver resolver = new KafkaDltDestinationResolver();

  @Test
  void apply_consumerRecord_returnsDltTopicPartition() {
    var record = new ConsumerRecord<>("link-verify", 2, 10L, "key", "value");

    var result = resolver.apply(record, new RuntimeException("boom"));

    assertThat(result.topic()).isEqualTo("link-verify.DLT");
    assertThat(result.partition()).isEqualTo(2);
  }

  @Test
  void apply_partitionUnknown_keepsPartition() {
    var record = new ConsumerRecord<>("link-categorize", 0, 0L, null, null);

    var result = resolver.apply(record, new IllegalStateException("boom"));

    assertThat(result.topic()).isEqualTo("link-categorize.DLT");
    assertThat(result.partition()).isEqualTo(0);
  }
}
