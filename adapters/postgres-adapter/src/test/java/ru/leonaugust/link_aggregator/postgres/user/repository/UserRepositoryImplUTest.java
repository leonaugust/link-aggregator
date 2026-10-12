package ru.leonaugust.link_aggregator.postgres.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.User;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.domain.user.UserWithLinks;
import ru.leonaugust.link_aggregator.postgres.user.dao.UserRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;
import ru.leonaugust.link_aggregator.postgres.user.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserRepositoryImplUTest {

  private static final String EMAIL = "test@mail.ru";

  @Mock
  private UserRepositoryDao dao;
  @Mock
  private UserMapper mapper;

  @InjectMocks
  private UserRepositoryImpl repository;

  @Test
  void getUserWithLinks_userFound_returnsMappedUser() {
    var criteria = criteria();
    var entity = UserEntity.builder().build();
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, EMAIL))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();
    var userWithLinks = new UserWithLinks(communication, List.of());
    when(dao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.of(entity));
    when(mapper.toDtoWithLinks(entity)).thenReturn(userWithLinks);

    assertThat(repository.getUserWithLinks(criteria)).contains(userWithLinks);
  }

  @Test
  void getUserWithLinks_userMissing_returnsEmpty() {
    when(dao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.empty());

    assertThat(repository.getUserWithLinks(criteria())).isEmpty();
  }

  @Test
  void save_user_persistsMappedEntity() {
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, EMAIL))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();
    var user = new User(communication);
    var entity = UserEntity.builder().build();
    when(mapper.toEntity(user)).thenReturn(entity);

    repository.save(user);

    verify(dao).save(entity);
  }

  private static UserSearchCriteria criteria() {
    return UserSearchCriteria.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, EMAIL))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();
  }
}
