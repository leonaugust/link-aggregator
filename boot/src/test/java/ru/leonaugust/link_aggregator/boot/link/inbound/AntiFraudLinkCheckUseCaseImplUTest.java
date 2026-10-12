package ru.leonaugust.link_aggregator.boot.link.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSafetyCheckPort;
import ru.leonaugust.link_aggregator.application.impl.link.inbound.AntiFraudLinkCheckUseCaseImpl;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkSafetyVerdict;
import ru.leonaugust.link_aggregator.domain.link.LinkSafetyViolationException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class AntiFraudLinkCheckUseCaseImplUTest {

  @Mock
  private LinkSafetyCheckPort linkSafetyCheckPort;

  @InjectMocks
  private AntiFraudLinkCheckUseCaseImpl useCase;

  @Test
  void check_nullLink_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> useCase.check(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void check_nullUrl_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> useCase.check(Link.builder().url(null).build()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void check_blankUrl_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> useCase.check(Link.builder().url("   ").build()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void check_malformedUrl_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> useCase.check(Link.builder().url("http://[invalid").build()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void check_safeVerdict_noExceptionAndVerdictCovered() {
    var verdict = LinkSafetyVerdict.safe("opentip");
    when(linkSafetyCheckPort.check("https://example.com/")).thenReturn(verdict);

    useCase.check(Link.builder().url("https://example.com/").build());

    verify(linkSafetyCheckPort).check("https://example.com/");
    // явные проверки record-методов ради JaCoCo-покрытия ядра
    assertThat(verdict).isEqualTo(LinkSafetyVerdict.safe("opentip"));
    assertThat(verdict.toString()).contains("suspicious=false").contains("opentip");
    assertThat(verdict.hashCode()).isEqualTo(LinkSafetyVerdict.safe("opentip").hashCode());
  }

  @Test
  void check_suspiciousVerdict_throwsLinkSafetyViolationException() {
    var verdict = LinkSafetyVerdict.violated("opentip", "threat=malware_download");
    when(linkSafetyCheckPort.check("https://example.com/")).thenReturn(verdict);

    var thrown = catchThrowableOfType(
        () -> useCase.check(Link.builder().url("https://example.com/").build()),
        LinkSafetyViolationException.class);

    assertThat(thrown).isNotNull();
    assertThat(thrown.getUrl()).isEqualTo("https://example.com/");
    assertThat(thrown.getSource()).isEqualTo("opentip");
    assertThat(thrown.getDetail()).isEqualTo("threat=malware_download");
    assertThat(thrown.getMessage())
        .contains("https://example.com/")
        .contains("opentip")
        .contains("threat=malware_download");
  }

  @Test
  void check_portThrowsRuntimeException_propagates() {
    when(linkSafetyCheckPort.check("https://example.com/"))
        .thenThrow(new IllegalStateException("provider boom"));

    assertThatThrownBy(() -> useCase.check(Link.builder().url("https://example.com/").build()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("provider boom");
  }
}