package ru.leonaugust.link_aggregator.domain.link;

import lombok.Builder;
import lombok.With;

@Builder(toBuilder = true)
@With
public record Link(String link, String category) {

}
