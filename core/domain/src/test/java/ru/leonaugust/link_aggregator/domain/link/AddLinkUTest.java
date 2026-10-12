package ru.leonaugust.link_aggregator.domain.link;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class AddLinkUTest {

  @Test
  void builderExposesLinksAndCriteria() {
    var link = Link.builder().url("https://example.com").category("development").build();
    var criteria = UserSearchCriteria.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, "test@mail.ru"))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();

    var addLink = AddLink.builder()
        .links(List.of(link))
        .userSearchCriteria(criteria)
        .build();

    assertThat(addLink.links()).containsExactly(link);
    assertThat(addLink.userSearchCriteria()).isEqualTo(criteria);
    assertThat(addLink.toString()).contains("https://example.com");
  }
}
