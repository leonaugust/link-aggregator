package ru.leonaugust.link_aggregator.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConditionalOnProperty(name = "link-aggregator.ai.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "link-aggregator.ai")
public record AiProperties(
        String baseUrl,
        String generateModel,
        String embedModel) {
}