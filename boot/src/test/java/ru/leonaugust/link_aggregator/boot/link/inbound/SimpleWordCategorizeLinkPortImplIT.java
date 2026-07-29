package ru.leonaugust.link_aggregator.boot.link.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static ru.leonaugust.link_aggregator.postgres.category.config.CategoryDataInitializer.CATEGORIES_INITIALIZED;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.leonaugust.link_aggregator.application.impl.link.inbound.SimpleWordCategorizeLinkUseCaseImpl;
import ru.leonaugust.link_aggregator.boot.FullContextITBase;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.postgres.category.config.CategoryDataInitializer;
import ru.leonaugust.link_aggregator.postgres.settings.dao.SettingsRepositoryDao;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    classes = MicroserviceStarter.class
)
public class SimpleWordCategorizeLinkPortImplIT extends FullContextITBase {

  private static final String DEVELOPMENT_TEXT = "java это какой-то текст, где spring backend архитектура --- ?? программирование код разработка."
      + "важно для frontend. Чистая архитектура Роберта Мартина. Инкапсуляция и полиморфизм. чистота кода и архитектурные границы. java kubernetes.";
  private static final String BOOK_TEXT = "какой-то текст. роман, где главный герой. Автор ?? хотел отразить в этом литература сюжет."
      + "Литература. Характер персонажа. Карточка героя и харизматичный злодей. Какой-то мусорный текст. Критики плохо восприняли работу. книга роман, главный герой. Автор. роман, книга. пьеса";

  private final SimpleWordCategorizeLinkUseCaseImpl simpleWordCategorizeLinkPort;
  private final SettingsRepositoryDao settingsRepositoryDao;
  private final CategoryDataInitializer categoryDataInitializer;

  @BeforeEach
  void beforeEach() {
    categoryDataInitializer.loadCategoriesFromFiles();
    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> settingsRepositoryDao.existsByName(CATEGORIES_INITIALIZED));
  }

  @AfterEach
  void afterEach() {
    settingsRepositoryDao.deleteAll();
  }

  @Test
  void categorize_should_returnCorrectCategory() {
    assertThat(
        simpleWordCategorizeLinkPort.categorize(DEVELOPMENT_TEXT)
    ).isEqualTo("development");
    assertThat(
        simpleWordCategorizeLinkPort.categorize(BOOK_TEXT)
    ).isEqualTo("book");
  }
}
