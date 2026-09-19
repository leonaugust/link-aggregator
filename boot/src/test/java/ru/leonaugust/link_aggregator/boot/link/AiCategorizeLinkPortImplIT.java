package ru.leonaugust.link_aggregator.boot.link;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import ru.leonaugust.link_aggregator.ai.category.AiCategorizeLinkUseCaseImpl;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.test_config.AiFullContextITBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    classes = MicroserviceStarter.class
)
public class AiCategorizeLinkPortImplIT extends AiFullContextITBase {

  private static final String DEVELOPMENT_TEXT = "java это какой-то текст, где spring backend архитектура --- ?? программирование код разработка."
      + "важно для frontend. Чистая архитектура Роберта Мартина. Инкапсуляция и полиморфизм. чистота кода и архитектурные границы. java kubernetes.";
  private static final String BOOK_TEXT = "какой-то текст. роман, где главный герой. Автор ?? хотел отразить в этом литература сюжет."
      + "Литература. Характер персонажа. Карточка героя и харизматичный злодей. Какой-то мусорный текст. Критики плохо восприняли работу. книга роман, главный герой. Автор. роман, книга. пьеса";

  private final AiCategorizeLinkUseCaseImpl categorizeLinkUseCase;

  @Test
  void categorize_should_returnCorrectCategory() {
    assertThat(
        categorizeLinkUseCase.categorize(DEVELOPMENT_TEXT).block()
    ).isEqualTo("development");
    assertThat(
        categorizeLinkUseCase.categorize(BOOK_TEXT).block()
    ).isEqualTo("book");
  }
}
