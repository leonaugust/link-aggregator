package ru.leonaugust.link_aggregator.application.impl.link.inbound;

import java.util.Comparator;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.application.UseCase;
import ru.leonaugust.link_aggregator.application.api.category.outbound.CategoryRepository;
import ru.leonaugust.link_aggregator.application.api.link.inbound.CategorizeLinkUseCase;
import ru.leonaugust.link_aggregator.domain.category.Category;
import ru.leonaugust.link_aggregator.domain.link.ContentPage;

@RequiredArgsConstructor
@Slf4j
public class SimpleWordCategorizeLinkUseCaseImpl implements CategorizeLinkUseCase {

  private static final String UNDEFINED = "undefined";
  private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}]{2,}");

  private final CategoryRepository categoryRepository;

  @Override
  public Mono<String> categorize(ContentPage contentPage) {
    return Mono.fromCallable(() -> categorizeSync(contentPage));
  }

  public String categorizeSync(ContentPage contentPage) {
    var categories = categoryRepository.getCategories();

    Set<String> pageWords = extractWords(contentPage);
    if (pageWords.isEmpty()) {
      log.info("Не удалось извлечь слова со страницы");
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
      log.info("Ни одна категория не подошла");
      return UNDEFINED;
    }
    long maxMatches = bestMatch.get().matchCount;

    String categoryName = bestMatch.get().category.name();
    log.info("Ссылка отнесена к категории '{}' (совпадений: {})", categoryName, maxMatches);
    return categoryName;
  }

  private Set<String> extractWords(ContentPage contentPage) {
    if (contentPage == null || contentPage.isEmpty()) {
      return Set.of();
    }
    return WORD_PATTERN.matcher(contentPage.text().toLowerCase())
        .results()
        .map(MatchResult::group)
        .collect(Collectors.toSet());
  }

  record CategoryMatch(Category category, long matchCount) {

  }
}
