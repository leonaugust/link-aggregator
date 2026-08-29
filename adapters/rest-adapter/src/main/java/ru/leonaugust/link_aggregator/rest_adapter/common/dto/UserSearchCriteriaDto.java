package ru.leonaugust.link_aggregator.rest_adapter.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;

@Builder
@Jacksonized
@Schema(name = "UserCommunicationRequestDto")
public record UserSearchCriteriaDto(
    Map<CommunicationChannel, String> contacts
) {

}
