package ru.leonaugust.link_aggregator.domain.link;

import java.util.UUID;
import lombok.Builder;
import lombok.With;

@Builder(toBuilder = true)
@With
public record Link(UUID id, String url, String category) {

}
