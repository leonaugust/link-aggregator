package ru.leonaugust.link_aggregator.application.api.link.inbound;

import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.domain.link.ContentPage;

public interface CategorizeLinkUseCase {

  Mono<String> categorize(ContentPage contentPage);
}
