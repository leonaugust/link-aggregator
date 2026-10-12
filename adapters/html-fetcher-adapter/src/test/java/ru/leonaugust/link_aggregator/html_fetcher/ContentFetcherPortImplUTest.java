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
  void fetchText_invalidUrl_returnsEmptySet() {
    assertThat(port.fetchText("not a valid url")).isEmpty();
  }
}
