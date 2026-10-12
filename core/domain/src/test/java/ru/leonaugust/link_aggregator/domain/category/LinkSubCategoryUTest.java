package ru.leonaugust.link_aggregator.domain.category;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class LinkSubCategoryUTest {

  @Test
  void values_containsNoCategoriesYet() {
    assertThat(LinkSubCategory.values()).isEmpty();
  }
}
