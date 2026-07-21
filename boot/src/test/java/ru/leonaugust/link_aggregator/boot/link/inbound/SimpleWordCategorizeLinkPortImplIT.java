package ru.leonaugust.link_aggregator.boot.link.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static ru.leonaugust.link_aggregator.postgres.category.config.CategoryDataInitializer.CATEGORIES_INITIALIZED;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.leonaugust.link_aggregator.application.impl.link.inbound.SimpleWordCategorizeLinkUseCaseImpl;
import ru.leonaugust.link_aggregator.boot.FullContextITBase;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.postgres.settings.dao.SettingsRepositoryDao;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    classes = MicroserviceStarter.class
)
public class SimpleWordCategorizeLinkPortImplIT extends FullContextITBase {

  private static final String DEVELOPMENT_LINK = "https://habr.com/ru/companies/skillfactory/articles/811769/";
  private static final String BOOK_LINK = "https://author.today/contest/41";

  private final SimpleWordCategorizeLinkUseCaseImpl simpleWordCategorizeLinkPort;
  private final SettingsRepositoryDao settingsRepositoryDao;

  @BeforeEach
  void beforeEach() {
    Awaitility.await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> settingsRepositoryDao.existsByName(CATEGORIES_INITIALIZED));
  }

  @Test
  void categorize_should_returnCorrectCategory() {
    assertThat(
        simpleWordCategorizeLinkPort.categorize(DEVELOPMENT_LINK)
    ).isEqualTo("development");
    assertThat(
        simpleWordCategorizeLinkPort.categorize(BOOK_LINK)
    ).isEqualTo("book");
  }
}
