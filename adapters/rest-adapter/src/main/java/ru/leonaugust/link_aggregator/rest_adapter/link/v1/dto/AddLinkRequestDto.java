package ru.leonaugust.link_aggregator.rest_adapter.link.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.UserSearchCriteriaDto;

@Builder
@Jacksonized
@Schema(name = "AddLinkRequestDto")
public record AddLinkRequestDto(
    List<String> links,
    UserSearchCriteriaDto userSearch
) {


}
