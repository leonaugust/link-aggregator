package ru.leonaugust.link_aggregator.ai.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.ai.common.AiClient;
import ru.leonaugust.link_aggregator.ai.common.GenerateRequest;
import ru.leonaugust.link_aggregator.ai.common.GenerateResponse;
import ru.leonaugust.link_aggregator.ai.config.AiProperties;
import ru.leonaugust.link_aggregator.domain.link.ContentPage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiCategorizeLinkUseCaseImplUTest {

    private static final AiProperties AI_PROPERTIES =
            new AiProperties("http://localhost:11434", "generate-model", "embed-model");

    @Mock
    private AiClient aiClient;

    private AiCategorizeLinkUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AiCategorizeLinkUseCaseImpl(aiClient, AI_PROPERTIES);
    }

    @Test
    void categorize_cleanText_returnsGeneratedCategory() {
        when(aiClient.generate(any(GenerateRequest.class)))
                .thenReturn(Mono.just(new GenerateResponse(" development ")));

        assertThat(useCase.categorize(new ContentPage("spring boot article")).block())
                .isEqualTo("development");
    }

    @Test
    void categorize_unknownCategory_returnsUndefined() {
        when(aiClient.generate(any(GenerateRequest.class)))
                .thenReturn(Mono.just(new GenerateResponse("weather")));

        assertThat(useCase.categorize(new ContentPage("weather forecast")).block())
                .isEqualTo("undefined");
    }

    @Test
    void categorize_emptyModelResponse_returnsUndefined() {
        when(aiClient.generate(any(GenerateRequest.class)))
                .thenReturn(Mono.just(new GenerateResponse(" ")));

        assertThat(useCase.categorize(new ContentPage("some text")).block())
                .isEqualTo("undefined");
    }

    @Test
    void categorize_emptyContent_sendsRequestAndReturnsGeneratedCategory() {
        when(aiClient.generate(any(GenerateRequest.class)))
                .thenReturn(Mono.just(new GenerateResponse("book")));

        assertThat(useCase.categorize(ContentPage.empty()).block())
                .isEqualTo("book");
    }

    @Test
    void categorize_nullContent_sendsRequestAndReturnsGeneratedCategory() {
        when(aiClient.generate(any(GenerateRequest.class)))
                .thenReturn(Mono.just(new GenerateResponse("book")));

        assertThat(useCase.categorize(null).block())
                .isEqualTo("book");
    }
}
