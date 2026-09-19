package ru.leonaugust.test_config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.RequiredArgsConstructor;
import org.hibernate.dialect.PostgreSQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import ru.leonaugust.link_aggregator.ai.config.AiProperties;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Класс помещён в этом пакете, чтобы не менять ComponentScan у MicroserviceStarter
 * и production код не знал о тестовых классах
 */
@TestConfiguration
@ActiveProfiles(profiles = "test")
@EmbeddedKafka(partitions = 1)
public class AiFullContextITBase {

  public static final String POSTGRES_IMAGE = "postgres:16-alpine";
  public static final WireMockConfiguration WIRE_MOCK_CONFIGURATION =
      WireMockSpring.options().dynamicPort().notifier(new Slf4jNotifier(true));
  public static final WireMockServer WIREMOCK = new WireMockServer(WIRE_MOCK_CONFIGURATION);

  static final DockerImageName POSTGRES_DOCKER_IMAGE = DockerImageName.parse(POSTGRES_IMAGE)
      .asCompatibleSubstituteFor("postgres");
  static final String DB_NAME = "link_aggregator";
  public static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(POSTGRES_DOCKER_IMAGE)
      .withDatabaseName(DB_NAME)
          .withUsername("postgres")
          .withPassword("postgres");
  private static final String GENERATE_MODEL = "qwen2.5:3b-instruct";
  private static final String EMBED_MODEL = "nomic-embed-text";

  @Autowired
  private AiProperties aiProperties;

  static int WIREMOCK_PORT;

  @BeforeAll
  static void beforeAll() throws IOException {
    WireMock.configureFor(WIREMOCK_PORT);
    awaitModelsReady(ollamaBaseUrl());
  }

  @DynamicPropertySource
  static void setUpProperties(final DynamicPropertyRegistry registry) {
    registry.add("link-aggregator.ai.base-url", AiFullContextITBase::ollamaBaseUrl);

    Startables.deepStart(POSTGRES_CONTAINER).join();

    bindDatasourceToPostgresOfTestContainers(registry, POSTGRES_CONTAINER);

    WIREMOCK.start();
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(WIREMOCK::isRunning);
    WIREMOCK_PORT = WIREMOCK.port();
    WireMock.configureFor(WIREMOCK_PORT);
    prepareForStart();

    bindUrlsToWiremock(registry, WIREMOCK::baseUrl);

    registry.add("spring.kafka.bootstrap-servers", () -> "${spring.embedded.kafka.brokers}");
  }

  private static String ollamaBaseUrl() {
    return System.getenv().getOrDefault("OLLAMA_URL", "http://localhost:11434");
  }

  private static void awaitModelsReady(String baseUrl) {
    Awaitility.await().atMost(2, TimeUnit.MINUTES)
        .pollInterval(2, TimeUnit.SECONDS)
        .until(() -> {
          try {
            var request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/tags")).GET().build();
            var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
              return false;
            }
            var modelNames = Pattern.compile("\"name\":\"[^\"]+\"")
                .matcher(response.body())
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());
            return modelNames.stream().anyMatch(name -> name.startsWith("\"name\":\"" + GENERATE_MODEL))
                && modelNames.stream().anyMatch(name -> name.startsWith("\"name\":\"" + EMBED_MODEL));
          } catch (Exception ignored) {
            return false;
          }
        });
  }

  public static void bindDatasourceToPostgresOfTestContainers(DynamicPropertyRegistry registry,
      PostgreSQLContainer<?> postgresContainer) {
    registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.jdbc-url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresContainer::getUsername);
    registry.add("spring.datasource.password", postgresContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
    registry.add("spring.jpa.properties.hibernate.dialect", PostgreSQLDialect.class::getName);
    registry.add("spring.jpa.show-sql", () -> false);
    registry.add("spring.jpa.properties.hibernate.format_sql", () -> false);
    registry.add("spring.jpa.properties.hibernate.use_sql_comments", () -> false);
  }

  public static void bindUrlsToWiremock(DynamicPropertyRegistry registry, Supplier<Object> baseUrlSupplier) {
    registry.add("some.integration.url", baseUrlSupplier);
  }

  private static void prepareForStart() {
  // some mocks required at startup
  }

  @AfterEach
  @SuppressWarnings("java:S5786")
  public void commonAfterEach() {
    WireMock.reset();
    prepareForStart();
  }
}