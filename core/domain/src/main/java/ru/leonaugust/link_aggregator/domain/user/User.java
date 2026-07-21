package ru.leonaugust.link_aggregator.domain.user;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record User(
    UserCommunication communication
) {

  public void validate() {
    if (communication == null) {
      throw new RuntimeException();
    }

    if (communication.preferredChannel() == null) {
      throw new RuntimeException();
    }

    if (communication.contacts() == null) {
      throw new RuntimeException();
    }

    var hasValidContact = Optional.of(communication.contacts())
        .map(Map::entrySet)
        .orElse(Set.of())
        .stream()
        .anyMatch(entry -> entry.getKey() == communication.preferredChannel() && entry.getValue() != null);
    if (!hasValidContact) {
      throw new RuntimeException();
    }
  }


}