package ru.leonaugust.link_aggregator.domain.settings;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record Settings(
    String name
) {

}