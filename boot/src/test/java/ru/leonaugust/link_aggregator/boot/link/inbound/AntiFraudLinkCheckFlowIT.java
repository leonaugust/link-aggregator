package ru.leonaugust.link_aggregator.boot.link.inbound;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.postgres.link.dao.LinkRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.user.dao.UserRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.user.entity.CommunicationChannelDbEnum;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserCommunicationEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserLanguageDbEnum;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.UserSearchCriteriaDto;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.controller.LinkV1Controller;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.dto.AddLinkRequestDto;
import ru.leonaugust.test_config.FullContextITBase;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MicroserviceStarter.class)
public class AntiFraudLinkCheckFlowIT extends FullContextITBase {

  public static final String EMAIL = "fraud-check@mail.ru";
  public static final String SAFE_URL = "https://safe.example.com/";
  public static final String MALICIOUS_URL = "https://malicious.example.com/";

  @Value("${server.servlet.context-path}")
  private String rootPath;
  @LocalServerPort
  private int port;

  private WebTestClient webTestClient;
  private final LinkRepositoryDao linkRepositoryDao;
  private final UserRepositoryDao userRepositoryDao;

  @PostConstruct
  void postConstruct() {
    webTestClient = WebTestClient
        .bindToServer().baseUrl("http://localhost:%s/%s".formatted(port, rootPath))
        .responseTimeout(Duration.ofHours(1L))
        .build();
  }

  @BeforeEach
  void registerOpenTipStubs() {
    // обе ссылки заканчиваются на "/", поэтому путь без сегмента и OpenTIP ищет их по домену
    stubFor(get(urlPathEqualTo("/search/domain"))
        .withQueryParam("request", equalTo("malicious.example.com"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Zone\":\"Red\"}")));
    stubFor(get(urlPathEqualTo("/search/domain"))
        .withQueryParam("request", equalTo("safe.example.com"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("{\"Zone\":\"Green\"}")));
  }

  @AfterEach
  void afterEach() {
    userRepositoryDao.deleteAll();
  }

  @Test
  void checkFlow_maliciousLinkBlocked_safeLinkSaved() {
    var userEntity = UserEntity.builder().build();
    userEntity.setCommunication(
        UserCommunicationEntity.builder()
            .preferredChannel(CommunicationChannelDbEnum.EMAIL)
            .language(UserLanguageDbEnum.ENG)
            .email(EMAIL)
            .build()
    );
    userRepositoryDao.save(userEntity);

    webTestClient.post()
        .uri(LinkV1Controller.PATH)
        .bodyValue(AddLinkRequestDto.builder()
            .userSearch(UserSearchCriteriaDto.builder()
                .contacts(Map.of(CommunicationChannel.EMAIL, EMAIL))
                .build())
            .urls(List.of(SAFE_URL, MALICIOUS_URL))
            .build())
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    Awaitility.await()
        .atMost(60, TimeUnit.SECONDS)
        .pollDelay(1, TimeUnit.SECONDS)
        .until(() -> linkRepositoryDao.findByEmailOrTelegram(EMAIL, null).size() == 1);

    var savedLinks = linkRepositoryDao.findByEmailOrTelegram(EMAIL, null);
    assertThat(savedLinks)
        .extracting("url")
        .containsExactly(SAFE_URL);
    assertThat(savedLinks.get(0).getCategory()).isEqualTo("undefined");
    assertThat(savedLinks)
        .extracting("url")
        .doesNotContain(MALICIOUS_URL);
  }
}