package ru.leonaugust.link_aggregator.kafka.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {

  private final KafkaTemplate<?, ?> kafkaTemplate;
  private final long retryIntervalMs;
  private final long maxRetries;

  public KafkaErrorHandlingConfig(
      KafkaTemplate<?, ?> kafkaTemplate,
      @Value("${link-aggregator.kafka.retry.interval-ms:1000}") long retryIntervalMs,
      @Value("${link-aggregator.kafka.retry.max-retries:2}") long maxRetries) {
    this.kafkaTemplate = kafkaTemplate;
    this.retryIntervalMs = retryIntervalMs;
    this.maxRetries = maxRetries;
  }

  @Bean
  public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
    return new DeadLetterPublishingRecoverer(kafkaTemplate, new KafkaDltDestinationResolver());
  }

  @Bean
  public DefaultErrorHandler kafkaCommonErrorHandler(DeadLetterPublishingRecoverer recoverer) {
    return new DefaultErrorHandler(recoverer, new FixedBackOff(retryIntervalMs, maxRetries));
  }
}
