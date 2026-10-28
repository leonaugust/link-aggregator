package ru.leonaugust.link_aggregator.postgres.category.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.leonaugust.link_aggregator.postgres.category.config.CategoryDataInitializer.CATEGORIES_INITIALIZED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.postgres.category.dao.CategoryRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.settings.dao.SettingsRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.settings.entity.SettingsEntity;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class CategoryDataInitializerUTest {

  @Mock
  private CategoryRepositoryDao categoryDao;
  @Mock
  private SettingsRepositoryDao settingsDao;

  @InjectMocks
  private CategoryDataInitializer initializer;

  @Test
  void loadCategoriesFromFiles_whenNotInitialized_savesInitializationFlag() {
    when(settingsDao.existsByName(CATEGORIES_INITIALIZED)).thenReturn(false);

    initializer.loadCategoriesFromFiles();

    verify(settingsDao).save(any(SettingsEntity.class));
  }

  @Test
  void loadCategoriesFromFiles_whenAlreadyInitialized_doesNotSaveFlagAgain() {
    when(settingsDao.existsByName(CATEGORIES_INITIALIZED)).thenReturn(true);

    initializer.loadCategoriesFromFiles();

    verify(settingsDao, never()).save(any(SettingsEntity.class));
  }
}
