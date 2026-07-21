package ru.leonaugust.link_aggregator.rest_adapter.report.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.UserSearchCriteriaDto;

@Builder
@Jacksonized
@Schema(name = "SendReportRequestDto")
public record SendReportRequestDto(
    UserSearchCriteriaDto userSearchRequestDto
) {

}
