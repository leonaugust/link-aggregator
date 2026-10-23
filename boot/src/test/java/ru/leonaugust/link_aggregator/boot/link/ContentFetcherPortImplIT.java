package ru.leonaugust.link_aggregator.boot.link;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
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
import ru.leonaugust.link_aggregator.html_fetcher.ContentFetcherPortImpl;
import ru.leonaugust.test_config.FullContextITBase;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {MicroserviceStarter.class})
public class ContentFetcherPortImplIT extends FullContextITBase {

  private final ContentFetcherPortImpl contentFetcherPort;

  @Test
  void fetch_should_extractTextFromPage() {
    stubFor(get(urlPathEqualTo("/page"))
        .willReturn(ok().withHeader("Content-Type", "text/html")
            .withBody("<html><body>привет spring java</body></html>")));

    var contentPage = contentFetcherPort.fetch(WIREMOCK.baseUrl() + "/page");

    assertThat(contentPage.text()).contains("привет", "spring", "java");
  }
}
