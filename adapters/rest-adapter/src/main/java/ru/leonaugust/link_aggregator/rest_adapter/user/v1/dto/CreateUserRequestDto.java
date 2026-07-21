package ru.leonaugust.link_aggregator.rest_adapter.user.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.CommunicationChannelRequestDto;

@Builder
@Jacksonized
@Schema(name = "CreateUserRequestDto")
public record CreateUserRequestDto(
    Map<CommunicationChannel, String> contacts,
    CommunicationChannelRequestDto preferredChannel,
    UserLanguageRequestDto language
) {

  public enum UserLanguageRequestDto {
    ENG,
    RU
  }


}
