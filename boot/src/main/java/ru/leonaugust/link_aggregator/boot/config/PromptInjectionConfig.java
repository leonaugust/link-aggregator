package ru.leonaugust.link_aggregator.boot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.ai.common.AiClient;
import ru.leonaugust.link_aggregator.ai.config.AiProperties;
import ru.leonaugust.link_aggregator.ai.config.PromptInjectionProperties;
import ru.leonaugust.link_aggregator.ai.injection.EmbeddingPromptInjectionDetector;
import ru.leonaugust.link_aggregator.application.api.link.outbound.PromptInjectionDetector;

/**
 * Детектор prompt injection нужен на этапе антифрод-проверки независимо от стратегии
 * категоризации, поэтому его бин не привязан к {@code category-determination.strategy}.
 * Когда AI выключен, используется заглушка, всегда возвращающая "не инъекция".
 */
@Configuration
public class PromptInjectionConfig {

    @Bean
    @ConditionalOnProperty(name = "link-aggregator.ai.enabled", havingValue = "true")
    public PromptInjectionDetector promptInjectionDetector(AiClient aiClient,
                                                           AiProperties aiProperties,
                                                           PromptInjectionProperties promptInjectionProperties) {
        return new EmbeddingPromptInjectionDetector(aiClient, aiProperties, promptInjectionProperties);
    }

    @Bean
    @ConditionalOnMissingBean(PromptInjectionDetector.class)
    public PromptInjectionDetector noOpPromptInjectionDetector() {
        return text -> Mono.just(false);
    }
}
