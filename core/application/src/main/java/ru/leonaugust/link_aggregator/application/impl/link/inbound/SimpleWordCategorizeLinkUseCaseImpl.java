package ru.leonaugust.link_aggregator.application.impl.link.inbound;

import java.util.Comparator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.leonaugust.link_aggregator.application.UseCase;
import ru.leonaugust.link_aggregator.application.api.category.outbound.CategoryRepository;
import ru.leonaugust.link_aggregator.application.api.link.inbound.CategorizeLinkUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;
import ru.leonaugust.link_aggregator.domain.category.Category;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SimpleWordCategorizeLinkUseCaseImpl implements CategorizeLinkUseCase {

  private static final String UNDEFINED = "undefined";

  private final ContentFetcherPort contentFetcherPort;
  private final CategoryRepository categoryRepository;

  @Override
  public String categorize(String link) {
    var categories = categoryRepository.getCategories();

    Set<String> pageWords = contentFetcherPort.fetchText(link);
    if (pageWords.isEmpty()) {
      log.info("Не удалось извлечь слова со страницы {}", link);
      return UNDEFINED;
    }

    var bestMatch = categories.stream()
        .map(cat -> {
          long matchCount = cat.words().stream()
              .filter(w -> pageWords.contains(w.toLowerCase()))
              .count();
          return new CategoryMatch(cat, matchCount);
        })
        .filter(cm -> cm.matchCount > 0)
        .peek(cm -> log.info("match: %s, category: %s".formatted(cm.matchCount, cm.category.name())))
        .filter(cm -> cm.matchCount() > 10)
        .max(Comparator.comparingLong(CategoryMatch::matchCount));

    if (bestMatch.isEmpty()) {
      log.info("Ни одна категория не подошла для {}", link);
      return UNDEFINED;
    }
    long maxMatches = bestMatch.get().matchCount;

    String categoryName = bestMatch.get().category.name();
    log.info("Ссылка {} отнесена к категории '{}' (совпадений: {})", link, categoryName, maxMatches);
    return categoryName;
  }


  record CategoryMatch(Category category, long matchCount) {

  }
}
