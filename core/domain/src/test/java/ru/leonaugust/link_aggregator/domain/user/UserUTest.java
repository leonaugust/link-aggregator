package ru.leonaugust.link_aggregator.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserUTest {

  @Test
  void validate_nullCommunication_throws() {
    assertThatThrownBy(() -> new User(null).validate())
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void validate_nullPreferredChannel_throws() {
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, "test@mail.ru"))
        .preferredChannel(null)
        .build();

    assertThatThrownBy(() -> new User(communication).validate())
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void validate_nullContacts_throws() {
    var communication = UserCommunication.builder()
        .contacts(null)
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();

    assertThatThrownBy(() -> new User(communication).validate())
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void validate_noContactForPreferredChannel_throws() {
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.TELEGRAM, "tg"))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();

    assertThatThrownBy(() -> new User(communication).validate())
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void validate_contactForPreferredChannelIsNull_throws() {
    var contacts = new HashMap<CommunicationChannel, String>();
    contacts.put(CommunicationChannel.EMAIL, null);
    var communication = UserCommunication.builder()
        .contacts(contacts)
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();

    assertThatThrownBy(() -> new User(communication).validate())
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void validate_validContactForPreferredChannel_passes() {
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, "test@mail.ru"))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();

    assertThatCode(() -> new User(communication).validate()).doesNotThrowAnyException();
  }

  @Test
  void recordAccessorsAndObjectMethods_areCovered() {
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, "test@mail.ru"))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();
    var user = new User(communication);
    var equalUser = new User(communication);

    assertThat(user.communication()).isEqualTo(communication);
    assertThat(user).isEqualTo(equalUser);
    assertThat(user.hashCode()).isEqualTo(equalUser.hashCode());
    assertThat(user.toString()).contains("communication");
  }
}
