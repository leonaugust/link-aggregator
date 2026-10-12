package ru.leonaugust.link_aggregator.boot.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Duration;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.fraud.config.FraudCheckProperties;
import ru.leonaugust.link_aggregator.fraud.opentip.OpenTipLinkSafetyCheckPortImpl;
import ru.leonaugust.link_aggregator.fraud.opentip.OpenTipLookupResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class OpenTipLinkSafetyCheckPortImplUTest {

  private static final String BASE_URL = "https://opentip.example/api/v1";
  private static final String URL = "https://example.com/a?b=c&d=e";
  private static final String API_KEY = "token";
  private static final Duration TIMEOUT = Duration.ofSeconds(5);
  private static final String SOURCE = "opentip";

  @Mock
  private WebClient webClient;
  @Mock
  private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
  @Mock
  private WebClient.RequestHeadersSpec requestHeadersSpec;
  @Mock
  private WebClient.ResponseSpec responseSpec;
  @Mock
  private UriBuilder uriBuilder;
  @Captor
  private ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor;

  @Test
  void check_zoneGreen_returnsSafeWithoutDetail() {
    stubSuccessResponse("Green");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    assertThat(verdict.detail()).isNull();
  }

  @Test
  void check_zoneRed_returnsSuspiciousWithZoneDetail() {
    stubSuccessResponse("Red");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isTrue();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    assertThat(verdict.detail()).isEqualTo("zone=RED");
  }

  @Test
  void check_zoneOrange_returnsSuspicious() {
    stubSuccessResponse("Orange");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isTrue();
    assertThat(verdict.detail()).contains("zone=ORANGE");
  }

  @Test
  void check_zoneRedWithDifferentCaseAndSpaces_returnsSuspicious() {
    stubSuccessResponse(" red ");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isTrue();
    assertThat(verdict.detail()).isEqualTo("zone=RED");
  }

  @Test
  void check_zoneYellow_returnsSafeAsNonBlocking() {
    stubSuccessResponse("Yellow");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    assertThat(verdict.detail()).isNull();
  }

  @Test
  void check_zoneGrey_returnsSafe() {
    stubSuccessResponse("Grey");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_unknownZoneValue_returnsSafeAndDoesNotCrash() {
    stubSuccessResponse("Blue");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    assertThat(verdict.detail()).isNull();
  }

  @Test
  void check_zoneAbsent_returnsSafe() {
    stubSuccessResponse(null);

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_emptyZoneValue_returnsSafe() {
    stubSuccessResponse("   ");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_emptyBody_returnsSafe() {
    stubRequest(API_KEY, Mono.empty());

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_urlWithoutPath_looksUpByDomain() {
    stubSuccessResponse("Green");

    var verdict = port(true, API_KEY).check("https://example.com");

    assertThat(verdict.suspicious()).isFalse();
    applyUriFunction();
    verify(uriBuilder).path("/search/domain");
    verify(uriBuilder).queryParam("request", "example.com");
  }

  @Test
  void check_urlWithRootPath_looksUpByDomain() {
    stubSuccessResponse("Green");

    var verdict = port(true, API_KEY).check("https://example.com/");

    assertThat(verdict.suspicious()).isFalse();
    applyUriFunction();
    verify(uriBuilder).path("/search/domain");
    verify(uriBuilder).queryParam("request", "example.com");
  }

  @Test
  void check_urlWithQuery_looksUpByUrlWithWholeRawValue() {
    stubSuccessResponse("Red");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isTrue();
    applyUriFunction();
    verify(uriBuilder).path("/search/url");
    verify(uriBuilder).queryParam("request", URL);
  }

  @Test
  void check_nullUrl_makesNoRequestAndReturnsSafe() {
    var verdict = port(true, API_KEY).check(null);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    verify(webClient, never()).get();
  }

  @Test
  void check_blankUrl_makesNoRequestAndReturnsSafe() {
    var verdict = port(true, API_KEY).check("   ");

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    verify(webClient, never()).get();
  }

  @Test
  void check_malformedUri_makesNoRequestAndReturnsSafe() {
    var verdict = port(true, API_KEY).check("http://[invalid");

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    verify(webClient, never()).get();
  }

  @Test
  void check_schemeLessUrl_makesNoRequestAndReturnsSafe() {
    var verdict = port(true, API_KEY).check("link1");

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    verify(webClient, never()).get();
  }

  @Test
  void check_urlLongerThan2000Chars_makesNoRequestAndReturnsSafe() {
    var tooLongUrl = "https://example.com/" + "a".repeat(2000);

    var verdict = port(true, API_KEY).check(tooLongUrl);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
    verify(webClient, never()).get();
  }

  @Test
  void check_unparseableUrl_failClosed_throwsIllegalStateException() {
    assertThatThrownBy(() -> port(false, API_KEY).check("link1"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("link1");
    verify(webClient, never()).get();
  }

  @Test
  void check_apiKeyConfigured_sendsApiKeyHeader() {
    stubSuccessResponse("Green");

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    verify(requestHeadersSpec, times(1)).header("x-api-key", API_KEY);
  }

  @Test
  void check_apiKeyBlank_doesNotSendApiKeyHeader() {
    stubRequest(" ", Mono.just(new OpenTipLookupResponse("Green")));

    var verdict = port(true, " ").check(URL);

    assertThat(verdict.suspicious()).isFalse();
    verify(requestHeadersSpec, never()).header(anyString(), anyString());
  }

  @Test
  void check_apiKeyNull_doesNotSendApiKeyHeader() {
    stubRequest(null, Mono.just(new OpenTipLookupResponse("Green")));

    var verdict = port(true, null).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    verify(requestHeadersSpec, never()).header(anyString(), anyString());
  }

  @Test
  void check_notFound_returnsSafeAsNoData() {
    stubRequest(API_KEY, statusError(404));

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_badRequest_failOpen_returnsSafe() {
    stubRequest(API_KEY, statusError(400));

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_badRequest_failClosed_rethrows() {
    stubRequest(API_KEY, statusError(400));

    assertThatThrownBy(() -> port(false, API_KEY).check(URL))
        .isInstanceOfSatisfying(WebClientResponseException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(400));
  }

  @Test
  void check_payloadTooLarge_failOpen_returnsSafe() {
    stubRequest(API_KEY, statusError(413));

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_payloadTooLarge_failClosed_rethrows() {
    stubRequest(API_KEY, statusError(413));

    assertThatThrownBy(() -> port(false, API_KEY).check(URL))
        .isInstanceOfSatisfying(WebClientResponseException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(413));
  }

  @Test
  void check_unauthorized_failOpen_returnsSafe() {
    stubRequest(API_KEY, statusError(401));

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_unauthorized_failClosed_rethrows() {
    stubRequest(API_KEY, statusError(401));

    assertThatThrownBy(() -> port(false, API_KEY).check(URL))
        .isInstanceOfSatisfying(WebClientResponseException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(401));
  }

  @Test
  void check_forbidden_failOpen_returnsSafe() {
    stubRequest(API_KEY, statusError(403));

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_forbidden_failClosed_rethrows() {
    stubRequest(API_KEY, statusError(403));

    assertThatThrownBy(() -> port(false, API_KEY).check(URL))
        .isInstanceOfSatisfying(WebClientResponseException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(403));
  }

  @Test
  void check_serverError_failOpen_returnsSafe() {
    stubRequest(API_KEY, statusError(500));

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_serverError_failClosed_rethrows() {
    stubRequest(API_KEY, statusError(500));

    assertThatThrownBy(() -> port(false, API_KEY).check(URL))
        .isInstanceOfSatisfying(WebClientResponseException.class,
            e -> assertThat(e.getStatusCode().value()).isEqualTo(500));
  }

  @Test
  void check_requestTimeout_failOpen_returnsSafe() {
    stubRequest(API_KEY, Mono.error(new IllegalStateException("Timeout on blocking read")));

    var verdict = port(true, API_KEY).check(URL);

    assertThat(verdict.suspicious()).isFalse();
    assertThat(verdict.source()).isEqualTo(SOURCE);
  }

  @Test
  void check_requestTimeout_failClosed_throwsTransportReason() {
    stubRequest(API_KEY, Mono.error(new IllegalStateException("Timeout on blocking read")));

    assertThatThrownBy(() -> port(false, API_KEY).check(URL))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Timeout")
        .hasMessageNotContaining("не удалось сформировать запрос проверки");
  }

  private OpenTipLinkSafetyCheckPortImpl port(boolean failOpen, String apiKey) {
    return new OpenTipLinkSafetyCheckPortImpl(webClient,
        new FraudCheckProperties(BASE_URL, apiKey, failOpen, TIMEOUT));
  }

  private void stubSuccessResponse(String zone) {
    stubRequest(API_KEY, Mono.just(new OpenTipLookupResponse(zone)));
  }

  private void stubRequest(String apiKey, Mono<OpenTipLookupResponse> response) {
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
    if (apiKey != null && !apiKey.isBlank()) {
      when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
    }
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(OpenTipLookupResponse.class)).thenReturn(response);
  }

  private void applyUriFunction() {
    when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
    when(uriBuilder.queryParam(anyString(), anyString())).thenReturn(uriBuilder);
    when(uriBuilder.build()).thenReturn(URI.create(BASE_URL));
    verify(requestHeadersUriSpec).uri(uriCaptor.capture());
    uriCaptor.getValue().apply(uriBuilder);
  }

  private static Mono<OpenTipLookupResponse> statusError(int status) {
    return Mono.error(new WebClientResponseException(status, "status " + status, null, null, null));
  }
}
