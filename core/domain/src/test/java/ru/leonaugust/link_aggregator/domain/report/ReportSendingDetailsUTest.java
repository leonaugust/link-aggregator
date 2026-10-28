package ru.leonaugust.link_aggregator.domain.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ReportSendingDetailsUTest {

  @Test
  void builderExposesCategories() {
    var category = new ReportSendingDetails.ReportCategory("development", List.of("https://a", "https://b"));
    var details = ReportSendingDetails.builder()
        .categories(List.of(category))
        .build();

    assertThat(details.categories()).containsExactly(category);
    assertThat(details.toString()).contains("development");
  }

  @Test
  void reportCategoryRecordExposesDataAndEquality() {
    var category = new ReportSendingDetails.ReportCategory("book", List.of("https://book"));
    var equalCategory = new ReportSendingDetails.ReportCategory("book", List.of("https://book"));

    assertThat(category.categoryName()).isEqualTo("book");
    assertThat(category.links()).containsExactly("https://book");
    assertThat(category).isEqualTo(equalCategory);
    assertThat(category.hashCode()).isEqualTo(equalCategory.hashCode());
    assertThat(category.toString()).contains("book");
  }
}
