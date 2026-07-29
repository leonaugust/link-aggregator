package ru.leonaugust.link_aggregator.boot.category.inbound;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static ru.leonaugust.link_aggregator.postgres.category.config.CategoryDataInitializer.CATEGORIES_INITIALIZED;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.leonaugust.link_aggregator.boot.FullContextITBase;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.postgres.category.dao.CategoryRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.settings.dao.SettingsRepositoryDao;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT,
    classes = MicroserviceStarter.class
)
public class CategoryDataInitializerIT extends FullContextITBase {

  private final SettingsRepositoryDao settingsRepositoryDao;
  private final CategoryRepositoryDao categoryRepositoryDao;

  @AfterEach
  void afterEach() {
    settingsRepositoryDao.deleteAll();
    categoryRepositoryDao.deleteAll();
  }

  @Test
  void applicationStarted_should_saveCategories() {
    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> settingsRepositoryDao.existsByName(CATEGORIES_INITIALIZED));

    assertAll(
        () -> assertTrue(categoryRepositoryDao.existsByName("development")),
        () -> assertTrue(categoryRepositoryDao.existsByName("book"))
    );
  }

}
