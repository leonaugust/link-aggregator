package ru.leonaugust.link_aggregator.domain.user;

import java.util.Map;
import lombok.Builder;
import lombok.With;

@Builder(toBuilder = true)
@With
public record UserCommunication(
    Map<CommunicationChannel, String> contacts,
    CommunicationChannel preferredChannel,
    UserLanguage language
) {

  public enum UserLanguage {
    ENG,
    RU
  }

}
