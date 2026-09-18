package ru.leonaugust.link_aggregator.ai.common;

import reactor.core.publisher.Mono;

public interface AiClient {

    Mono<GenerateResponse> generate(GenerateRequest generateRequest);

    Mono<EmbedResponse> embed(EmbedRequest embedRequest);
}