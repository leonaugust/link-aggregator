package ru.leonaugust.link_aggregator.domain.user;

import java.util.Map;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record UserSearchCriteria(
    Map<CommunicationChannel, String> contacts,
    CommunicationChannel preferredChannel
) {

  public String getEmail() {
    return contacts.get(CommunicationChannel.EMAIL);
  }

  public String getTelegram() {
    return contacts.get(CommunicationChannel.TELEGRAM);
  }

}