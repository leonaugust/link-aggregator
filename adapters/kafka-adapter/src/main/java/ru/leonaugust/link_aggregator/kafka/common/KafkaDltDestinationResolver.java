package ru.leonaugust.link_aggregator.kafka.common;

import java.util.function.BiFunction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

public class KafkaDltDestinationResolver
    implements BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> {

  @Override
  public TopicPartition apply(ConsumerRecord<?, ?> record, Exception exception) {
    return new TopicPartition(record.topic() + KafkaTopicConfig.DLT_SUFFIX, record.partition());
  }
}
