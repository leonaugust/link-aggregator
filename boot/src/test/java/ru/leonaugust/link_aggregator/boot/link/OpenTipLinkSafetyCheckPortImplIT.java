package ru.leonaugust.link_aggregator.boot.link;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.fraud.opentip.OpenTipLinkSafetyCheckPortImpl;
import ru.leonaugust.test_config.FullContextITBase;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MicroserviceStarter.class)
public class OpenTipLinkSafetyCheckPortImplIT extends FullContextITBase {

  private static final String SOURCE = "opentip";
  private static final String URL = "https://example.com/a?b=c&d=e";

  private final OpenTipLinkSafetyCheckPortImpl openTipLinkSafetyCheckPort;

  private static void stubUrlLookup(int status, String body) {
    var response = aResponse().withStatus(status);
    if (body != null) {
      response = response.withHeader("Content-Type", "application/json").withBody(body);
    }
    stubFor(get(urlPathEqualTo("/search/url"))
        .withQueryParam("request", equalTo(URL))
        .willReturn(response));
  }

  @Test
  void check_zoneGreen_returnsSafeWithoutDetail() {
    stubUrlLookup(200, "{\"Zone\":\"Green\"}");

    var verdict = openTipLinkSafetyCheckPort.check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    assertThat(verdict.detail()).isNull();
  }

  @Test
  void check_zoneRed_returnsSuspiciousWithZoneDetail() {
    stubUrlLookup(200, "{\"Zone\":\"Red\"}");

    var verdict = openTipLinkSafetyCheckPort.check(URL);

    assertThat(verdict.suspicious()).isTrue();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    assertThat(verdict.detail()).isEqualTo("zone=RED");
  }

  @Test
  void check_unauthorized_returnsSafeAsFailOpenFallback() {
    stubUrlLookup(401, null);

    var verdict = openTipLinkSafetyCheckPort.check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_serverError_returnsSafeAsFailOpenFallback() {
    stubUrlLookup(500, null);

    var verdict = openTipLinkSafetyCheckPort.check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }
}
