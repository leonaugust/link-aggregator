package ru.leonaugust.link_aggregator.html_fetcher;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ContentFetcherPortImplUTest {

  private final ContentFetcherPortImpl port = new ContentFetcherPortImpl();

  @Test
  void fetch_invalidUrl_returnsEmptyPage() {
    assertThat(port.fetch("not a valid url").isEmpty()).isTrue();
  }

  @Test
  void truncate_textLongerThanLimit_returnsTruncatedText() {
    var text = "a".repeat(ContentFetcherPortImpl.MAX_CONTENT_LENGTH + 100);

    var result = ContentFetcherPortImpl.truncate(text);

    assertThat(result).hasSize(ContentFetcherPortImpl.MAX_CONTENT_LENGTH);
  }

  @Test
  void truncate_textWithinLimit_returnsOriginalText() {
    var text = "a".repeat(ContentFetcherPortImpl.MAX_CONTENT_LENGTH);

    var result = ContentFetcherPortImpl.truncate(text);

    assertThat(result).isEqualTo(text);
  }
}
