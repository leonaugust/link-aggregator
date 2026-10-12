package ru.leonaugust.link_aggregator.postgres.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.postgres.user.entity.CommunicationChannelDbEnum;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserCommunicationEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserLanguageDbEnum;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserMapperUTest {

  private final UserMapper mapper = new UserMapperImpl();

  @Test
  void toDtoCommunication_emailAndTelegram_mapsBothContacts() {
    var entity = UserCommunicationEntity.builder()
        .email("test@mail.ru")
        .telegram("tg")
        .preferredChannel(CommunicationChannelDbEnum.EMAIL)
        .language(UserLanguageDbEnum.ENG)
        .build();

    var communication = mapper.toDtoCommunication(entity);

    assertThat(communication.contacts())
        .containsEntry(CommunicationChannel.EMAIL, "test@mail.ru")
        .containsEntry(CommunicationChannel.TELEGRAM, "tg");
    assertThat(communication.preferredChannel()).isEqualTo(CommunicationChannel.EMAIL);
    assertThat(communication.language()).isEqualTo(UserCommunication.UserLanguage.ENG);
  }

  @Test
  void toDtoCommunication_noContacts_mapsEmptyContacts() {
    var entity = UserCommunicationEntity.builder()
        .preferredChannel(CommunicationChannelDbEnum.TELEGRAM)
        .language(UserLanguageDbEnum.RU)
        .build();

    var communication = mapper.toDtoCommunication(entity);

    assertThat(communication.contacts()).isEmpty();
    assertThat(communication.preferredChannel()).isEqualTo(CommunicationChannel.TELEGRAM);
    assertThat(communication.language()).isEqualTo(UserCommunication.UserLanguage.RU);
  }

  @Test
  void toCommunicationEntity_communication_mapsFieldsAndEnums() {
    var communication = UserCommunication.builder()
        .contacts(Map.of(
            CommunicationChannel.EMAIL, "test@mail.ru",
            CommunicationChannel.TELEGRAM, "tg"))
        .preferredChannel(CommunicationChannel.EMAIL)
        .language(UserCommunication.UserLanguage.RU)
        .build();

    var entity = mapper.toCommunicationEntity(communication);

    assertThat(entity.getEmail()).isEqualTo("test@mail.ru");
    assertThat(entity.getTelegram()).isEqualTo("tg");
    assertThat(entity.getPreferredChannel()).isEqualTo(CommunicationChannelDbEnum.EMAIL);
    assertThat(entity.getLanguage()).isEqualTo(UserLanguageDbEnum.RU);
  }
}
