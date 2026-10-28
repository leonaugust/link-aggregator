package ru.leonaugust.link_aggregator.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "link-aggregator.ai.prompt-injection")
public record PromptInjectionProperties(@DefaultValue("0.88") double threshold,
                                        @DefaultValue("300") int maxWords,
                                        @DefaultValue("2s") Duration embedTimeout,
                                        @DefaultValue("10s") Duration detectionTimeout) {
}
