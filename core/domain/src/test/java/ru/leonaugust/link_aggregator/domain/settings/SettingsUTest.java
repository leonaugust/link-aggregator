package ru.leonaugust.link_aggregator.domain.settings;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SettingsUTest {

  @Test
  void builderExposesNameAndEquality() {
    var settings = Settings.builder().name("categories-initialized").build();
    var equalSettings = Settings.builder().name("categories-initialized").build();

    assertThat(settings.name()).isEqualTo("categories-initialized");
    assertThat(settings).isEqualTo(equalSettings);
    assertThat(settings.hashCode()).isEqualTo(equalSettings.hashCode());
    assertThat(settings.toString()).contains("categories-initialized");
  }
}
