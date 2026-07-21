package ru.leonaugust.link_aggregator.postgres.category.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.leonaugust.link_aggregator.postgres.category.dao.CategoryRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.category.entity.CategoryEntity;
import ru.leonaugust.link_aggregator.postgres.settings.dao.SettingsRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.settings.entity.SettingsEntity;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryDataInitializer {

  public static final String CATEGORIES_INITIALIZED = "categories_initialized";

  private final CategoryRepositoryDao categoryDao;
  private final SettingsRepositoryDao settingsDao;

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void loadCategoriesFromFiles() {
    log.info("Category initialization has started");
    try {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resolver.getResources("classpath:categories/*/*.txt");

      for (Resource resource : resources) {
        // Получаем путь: "categories/development/en.txt"
        String path = resource.getURL().getPath();
        String[] parts = path.split("/");
        String categoryName = parts[parts.length - 2];   // "development"

        String content = readContent(resource);
        List<String> words = parseWords(content);
        for (var word : words) {
          var saved = categoryDao.existsByNameAndWord(categoryName, word);
          if (!saved) {
            categoryDao.save(CategoryEntity.builder()
                .name(categoryName)
                .word(word)
                .build());
          }
        }
      }
      settingsDao.save(SettingsEntity.builder()
          .name(CATEGORIES_INITIALIZED)
          .build());
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize category words", e);
    }
    log.info("Category initialization has finished");
  }

  private String readContent(Resource resource) throws Exception {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining(" "));
    }
  }

  private List<String> parseWords(String content) {
    return Arrays.stream(content.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(String::toLowerCase)
        .collect(Collectors.toList());
  }
}