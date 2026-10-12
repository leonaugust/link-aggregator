package ru.leonaugust.link_aggregator.application.impl.link.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.application.api.category.outbound.CategoryRepository;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;
import ru.leonaugust.link_aggregator.domain.category.Category;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SimpleWordCategorizeLinkUseCaseImplUTest {

  private static final String LINK = "https://example.com";

  @Mock
  private ContentFetcherPort contentFetcherPort;
  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private SimpleWordCategorizeLinkUseCaseImpl useCase;

  @Test
  void categorize_emptyPageWords_returnsUndefined() {
    when(categoryRepository.getCategories()).thenReturn(List.of());
    when(contentFetcherPort.fetchText(LINK)).thenReturn(Set.of());

    assertThat(useCase.categorize(LINK).block()).isEqualTo("undefined");
  }

  @Test
  void categorize_matchesNotMoreThanTenWords_returnsUndefined() {
    var category = Category.builder().name("development").words(List.of("java", "spring", "kotlin")).build();
    when(categoryRepository.getCategories()).thenReturn(List.of(category));
    when(contentFetcherPort.fetchText(LINK)).thenReturn(Set.of("java", "spring"));

    assertThat(useCase.categorize(LINK).block()).isEqualTo("undefined");
  }

  @Test
  void categorize_moreThanTenMatches_returnsCategoryName() {
    var words = List.of("java", "spring", "kotlin", "maven", "gradle", "jpa",
        "kafka", "reactor", "rest", "docker", "kubernetes");
    var category = Category.builder().name("development").words(words).build();
    when(categoryRepository.getCategories()).thenReturn(List.of(category));
    when(contentFetcherPort.fetchText(LINK)).thenReturn(Set.copyOf(words));

    assertThat(useCase.categorize(LINK).block()).isEqualTo("development");
  }

  @Test
  void categorize_noCategories_returnsUndefined() {
    when(categoryRepository.getCategories()).thenReturn(List.of());
    when(contentFetcherPort.fetchText(LINK)).thenReturn(Set.of("java", "spring"));

    assertThat(useCase.categorize(LINK).block()).isEqualTo("undefined");
  }
}
