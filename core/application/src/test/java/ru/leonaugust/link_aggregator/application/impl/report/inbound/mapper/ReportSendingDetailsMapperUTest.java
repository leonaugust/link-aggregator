package ru.leonaugust.link_aggregator.application.impl.report.inbound.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.report.ReportSendingDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ReportSendingDetailsMapperUTest {

  private final ReportSendingDetailsMapper mapper = new ReportSendingDetailsMapperImpl();

  @Test
  void toDto_linksProvided_groupsByCategory() {
    var links = List.of(
        Link.builder().url("https://a").category("development").build(),
        Link.builder().url("https://b").category("development").build(),
        Link.builder().url("https://c").category("book").build()
    );

    var result = mapper.toDto(links);

    assertThat(result.categories()).hasSize(2)
        .anySatisfy(category -> {
          assertThat(category.categoryName()).isEqualTo("development");
          assertThat(category.links()).containsExactlyInAnyOrder("https://a", "https://b");
        })
        .anySatisfy(category -> {
          assertThat(category.categoryName()).isEqualTo("book");
          assertThat(category.links()).containsExactly("https://c");
        });
  }

  @Test
  void toDto_emptyLinks_returnsDetailsWithoutCategories() {
    ReportSendingDetails result = mapper.toDto(List.of());

    assertThat(result.categories()).isEmpty();
  }
}
