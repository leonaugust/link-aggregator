package ru.leonaugust.link_aggregator.boot.link;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import ru.leonaugust.link_aggregator.fraud.opentip.OpenTipZone;

class OpenTipZoneUTest {

  @Test
  void from_knownZoneCaseInsensitive_returnsZone() {
    assertThat(OpenTipZone.from("Red")).isEqualTo(OpenTipZone.RED);
    assertThat(OpenTipZone.from("ORANGE")).isEqualTo(OpenTipZone.ORANGE);
    assertThat(OpenTipZone.from(" yellow ")).isEqualTo(OpenTipZone.YELLOW);
    assertThat(OpenTipZone.from("green")).isEqualTo(OpenTipZone.GREEN);
    assertThat(OpenTipZone.from("Grey")).isEqualTo(OpenTipZone.GREY);
  }

  @Test
  void from_nullOrBlankOrUnknown_returnsUnknown() {
    assertThat(OpenTipZone.from(null)).isEqualTo(OpenTipZone.UNKNOWN);
    assertThat(OpenTipZone.from("")).isEqualTo(OpenTipZone.UNKNOWN);
    assertThat(OpenTipZone.from("   ")).isEqualTo(OpenTipZone.UNKNOWN);
    assertThat(OpenTipZone.from("Blue")).isEqualTo(OpenTipZone.UNKNOWN);
  }

  @Test
  void blocking_onlyRedAndOrangeAreBlocking() {
    assertThat(OpenTipZone.RED.blocking()).isTrue();
    assertThat(OpenTipZone.ORANGE.blocking()).isTrue();
    assertThat(OpenTipZone.YELLOW.blocking()).isFalse();
    assertThat(OpenTipZone.GREEN.blocking()).isFalse();
    assertThat(OpenTipZone.GREY.blocking()).isFalse();
    assertThat(OpenTipZone.UNKNOWN.blocking()).isFalse();
  }
}