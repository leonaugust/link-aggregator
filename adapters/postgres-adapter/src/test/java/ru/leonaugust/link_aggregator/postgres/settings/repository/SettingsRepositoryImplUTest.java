package ru.leonaugust.link_aggregator.postgres.settings.repository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.domain.settings.Settings;
import ru.leonaugust.link_aggregator.postgres.settings.dao.SettingsRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.settings.entity.SettingsEntity;
import ru.leonaugust.link_aggregator.postgres.settings.mapper.SettingsMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SettingsRepositoryImplUTest {

  @Mock
  private SettingsRepositoryDao dao;
  @Mock
  private SettingsMapper mapper;

  @InjectMocks
  private SettingsRepositoryImpl repository;

  @Test
  void save_settings_persistsMappedEntity() {
    var settings = Settings.builder().name("categories-initialized").build();
    var entity = SettingsEntity.builder().name("categories-initialized").build();
    when(mapper.toEntity(settings)).thenReturn(entity);

    repository.save(settings);

    verify(dao).save(entity);
  }
}
