package ru.leonaugust.link_aggregator.domain.category;

import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record Category(
    String name,
    List<String> words
) {


}