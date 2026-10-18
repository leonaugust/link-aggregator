package ru.leonaugust.link_aggregator.postgres.link.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.postgres.link.dao.LinkRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.link.entity.LinkEntity;
import ru.leonaugust.link_aggregator.postgres.link.mapper.LinkMapper;
import ru.leonaugust.link_aggregator.postgres.user.dao.UserRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class LinkRepositoryImplUTest {

  private static final String EMAIL = "test@mail.ru";

  @Mock
  private LinkRepositoryDao linkRepositoryDao;
  @Mock
  private UserRepositoryDao userRepositoryDao;
  @Mock
  private LinkMapper mapper;

  @InjectMocks
  private LinkRepositoryImpl repository;

  @Test
  void save_userFound_persistsEntityAndReturnsDto() {
    var link = Link.builder().url("https://a").category("development").build();
    var criteria = criteria();
    var userId = UUID.randomUUID();
    var user = UserEntity.builder().id(userId).build();
    var entity = LinkEntity.builder().url("https://a").build();
    when(userRepositoryDao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.of(user));
    when(linkRepositoryDao.findByUserIdAndUrl(userId, "https://a")).thenReturn(Optional.empty());
    when(mapper.toEntity(link, user)).thenReturn(entity);
    when(linkRepositoryDao.save(entity)).thenReturn(entity);
    when(mapper.toDto(entity)).thenReturn(link);

    var result = repository.save(link, criteria);

    assertThat(result).isEqualTo(link);
    verify(linkRepositoryDao).save(entity);
  }

  @Test
  void save_existingLink_returnsExistingAndSkipsInsert() {
    var link = Link.builder().url("https://a").category("development").build();
    var userId = UUID.randomUUID();
    var user = UserEntity.builder().id(userId).build();
    var existing = LinkEntity.builder().id(UUID.randomUUID()).url("https://a").build();
    when(userRepositoryDao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.of(user));
    when(linkRepositoryDao.findByUserIdAndUrl(userId, "https://a")).thenReturn(Optional.of(existing));
    when(mapper.toDto(existing)).thenReturn(link);

    var result = repository.save(link, criteria());

    assertThat(result).isEqualTo(link);
    verify(linkRepositoryDao, never()).save(any());
    verify(mapper, never()).toEntity(any(), any());
  }

  @Test
  void save_concurrentDuplicate_reReadsAfterConstraintViolation() {
    var link = Link.builder().url("https://a").category("development").build();
    var userId = UUID.randomUUID();
    var user = UserEntity.builder().id(userId).build();
    var entity = LinkEntity.builder().url("https://a").build();
    var existing = LinkEntity.builder().id(UUID.randomUUID()).url("https://a").build();
    when(userRepositoryDao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.of(user));
    when(linkRepositoryDao.findByUserIdAndUrl(userId, "https://a"))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(existing));
    when(mapper.toEntity(link, user)).thenReturn(entity);
    when(linkRepositoryDao.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate"));
    when(mapper.toDto(existing)).thenReturn(link);

    var result = repository.save(link, criteria());

    assertThat(result).isEqualTo(link);
  }

  @Test
  void save_userMissing_throws() {
    var link = Link.builder().url("https://a").build();
    when(userRepositoryDao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> repository.save(link, criteria()))
        .isInstanceOf(NoSuchElementException.class);
    verifyNoInteractions(linkRepositoryDao, mapper);
  }

  @Test
  void update_existingLink_mergesAndSaves() {
    var id = UUID.randomUUID();
    var link = Link.builder().url("https://b").category("book").build();
    var existing = LinkEntity.builder().id(id).url("https://a").build();
    when(linkRepositoryDao.findById(id)).thenReturn(Optional.of(existing));

    repository.update(id, link);

    verify(mapper).merge(existing, link);
    verify(linkRepositoryDao).save(existing);
  }

  @Test
  void update_linkMissing_throws() {
    var id = UUID.randomUUID();
    when(linkRepositoryDao.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> repository.update(id, Link.builder().url("https://b").build()))
        .isInstanceOf(NoSuchElementException.class);
    verify(mapper, never()).merge(any(), any());
  }

  @Test
  void saveAll_emptyLinks_savesNothing() {
    var user = userEntityWithLinks();
    when(userRepositoryDao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.of(user));

    repository.saveAll(List.of(), criteria());

    verify(linkRepositoryDao, never()).save(any());
    verify(userRepositoryDao, never()).save(any());
  }

  @Test
  void saveAll_newLink_addsLinkToUserAndSavesUser() {
    var user = userEntityWithLinks();
    when(userRepositoryDao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.of(user));

    repository.saveAll(List.of(Link.builder().url("https://a").category("development").build()), criteria());

    assertThat(user.getLinks()).hasSize(1);
    assertThat(user.getLinks().get(0).getUrl()).isEqualTo("https://a");
    assertThat(user.getLinks().get(0).getCategory()).isEqualTo("development");
    assertThat(user.getLinks().get(0).getUser()).isEqualTo(user);
    verify(userRepositoryDao).save(user);
  }

  @Test
  void saveAll_existingLink_doesNotDuplicateButSavesUser() {
    var user = userEntityWithLinks();
    user.getLinks().add(LinkEntity.builder().url("https://a").build());
    when(userRepositoryDao.findByEmailOrTelegram(EMAIL, null)).thenReturn(Optional.of(user));

    repository.saveAll(List.of(Link.builder().url("https://a").category("development").build()), criteria());

    assertThat(user.getLinks()).hasSize(1);
    verify(userRepositoryDao).save(user);
  }

  @Test
  void getByUserDetails_mapsEachEntityToDto() {
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, EMAIL))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();
    var entity = LinkEntity.builder().url("https://a").build();
    var link = Link.builder().url("https://a").category("development").build();
    when(linkRepositoryDao.findByEmailOrTelegram(EMAIL, null)).thenReturn(List.of(entity));
    when(mapper.toDto(entity)).thenReturn(link);

    assertThat(repository.getByUserDetails(communication)).containsExactly(link);
  }

  private static UserEntity userEntityWithLinks() {
    var user = UserEntity.builder().build();
    user.setLinks(new ArrayList<>());
    return user;
  }

  private static UserSearchCriteria criteria() {
    return UserSearchCriteria.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, EMAIL))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();
  }
}
