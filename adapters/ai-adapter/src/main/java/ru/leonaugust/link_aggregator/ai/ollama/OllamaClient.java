package ru.leonaugust.link_aggregator.ai.ollama;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.ai.common.*;

@Component
@ConditionalOnProperty(name = "link-aggregator.category-determination.strategy", havingValue = "ai", matchIfMissing = false)
@ConditionalOnProperty(name = "link-aggregator.ai.ollama.enabled", havingValue = "true", matchIfMissing = false)
public class OllamaClient implements AiClient {

    private static final String GENERATE_ENDPOINT = "/api/generate";
    private static final String EMBED_ENDPOINT = "/api/embeddings";

    private final WebClient webClient;

    public OllamaClient(WebClient ollamaWebClient) {
        this.webClient = ollamaWebClient;
    }

    @Override
    public Mono<GenerateResponse> generate(GenerateRequest generateRequest) {
        return webClient.post()
                .uri(GENERATE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(generateRequest)
                .retrieve()
                .bodyToMono(GenerateResponse.class);
    }

    @Override
    public Mono<EmbedResponse> embed(EmbedRequest embedRequest) {
        return webClient.post()
                .uri(EMBED_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(embedRequest)
                .retrieve()
                .bodyToMono(EmbedResponse.class);
    }

}