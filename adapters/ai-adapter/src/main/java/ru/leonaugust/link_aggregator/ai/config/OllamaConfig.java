package ru.leonaugust.link_aggregator.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@ConditionalOnProperty(name = "link-aggregator.ai.enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties({AiProperties.class, PromptInjectionProperties.class})
public class OllamaConfig {

    @Bean
    public WebClient ollamaWebClient(AiProperties props) {
        return WebClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }
}