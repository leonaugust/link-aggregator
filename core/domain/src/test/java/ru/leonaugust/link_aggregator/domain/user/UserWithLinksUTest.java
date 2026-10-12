package ru.leonaugust.link_aggregator.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.domain.link.Link;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserWithLinksUTest {

  @Test
  void constructorExposesDataAndWithMethodsProduceCopies() {
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, "test@mail.ru"))
        .preferredChannel(CommunicationChannel.EMAIL)
        .language(UserCommunication.UserLanguage.ENG)
        .build();
    var links = List.of(Link.builder().url("https://example.com").build());
    var userWithLinks = new UserWithLinks(communication, links);

    var updatedLinks = List.of(Link.builder().url("https://other.com").build());
    var updatedCommunication = communication.toBuilder()
        .language(UserCommunication.UserLanguage.RU)
        .build();

    assertThat(userWithLinks.communicationDetails()).isEqualTo(communication);
    assertThat(userWithLinks.links()).containsExactlyElementsOf(links);
    assertThat(userWithLinks.withLinks(updatedLinks).links()).containsExactlyElementsOf(updatedLinks);
    assertThat(userWithLinks.withCommunicationDetails(updatedCommunication).communicationDetails())
        .isEqualTo(updatedCommunication);
    assertThat(userWithLinks.toString()).contains("links");
  }
}
