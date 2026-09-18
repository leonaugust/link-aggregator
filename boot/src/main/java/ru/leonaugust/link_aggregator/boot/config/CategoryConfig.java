package ru.leonaugust.link_aggregator.boot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.leonaugust.link_aggregator.ai.category.AiCategorizeLinkUseCaseImpl;
import ru.leonaugust.link_aggregator.ai.common.AiClient;
import ru.leonaugust.link_aggregator.ai.config.AiProperties;
import ru.leonaugust.link_aggregator.application.api.category.outbound.CategoryRepository;
import ru.leonaugust.link_aggregator.application.api.link.inbound.CategorizeLinkUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;
import ru.leonaugust.link_aggregator.application.impl.link.inbound.SimpleWordCategorizeLinkUseCaseImpl;

@Configuration
public class CategoryConfig {

    @Bean
    @ConditionalOnProperty(name = "link-aggregator.category-determination.strategy", havingValue = "simple", matchIfMissing = true)
    public CategorizeLinkUseCase simpleCategorizeLinkUseCase(
            ContentFetcherPort contentFetcherPort,
            CategoryRepository categoryRepository) {
        return new SimpleWordCategorizeLinkUseCaseImpl(contentFetcherPort, categoryRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "link-aggregator.category-determination.strategy", havingValue = "ai")
    public CategorizeLinkUseCase aiCategorizeLinkUseCase(AiClient aiClient,
                                                         AiProperties aiProperties,
                                                         ContentFetcherPort contentFetcherPort) {
        return new AiCategorizeLinkUseCaseImpl(aiClient, aiProperties, contentFetcherPort);
    }
}