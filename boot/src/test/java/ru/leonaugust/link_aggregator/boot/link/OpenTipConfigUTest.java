package ru.leonaugust.link_aggregator.boot.link;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import ru.leonaugust.link_aggregator.fraud.config.FraudCheckProperties;
import ru.leonaugust.link_aggregator.fraud.config.OpenTipConfig;

class OpenTipConfigUTest {

  private static FraudCheckProperties properties(String apiKey) {
    return new FraudCheckProperties("https://opentip.kaspersky.com/api/v1", apiKey, true,
        Duration.ofSeconds(10));
  }

  @Test
  void openTipWebClient_blankApiKey_throwsIllegalStateException() {
    assertThatThrownBy(() -> new OpenTipConfig().openTipWebClient(properties("  ")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("OPENTIP_API_KEY")
        .hasMessageContaining("link-aggregator.fraud-check.api-key");
  }

  @Test
  void openTipWebClient_nullApiKey_throwsIllegalStateException() {
    assertThatThrownBy(() -> new OpenTipConfig().openTipWebClient(properties(null)))
        .isInstanceOf(IllegalStateException.class);
  }
}