package ru.leonaugust.link_aggregator.application.api.link.inbound;

import reactor.core.publisher.Mono;

public interface CategorizeLinkUseCase {

  Mono<String> categorize(String link);

}
