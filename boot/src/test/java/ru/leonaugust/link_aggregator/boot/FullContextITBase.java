package ru.leonaugust.link_aggregator.boot;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.hibernate.dialect.PostgreSQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;

@TestConfiguration
@ActiveProfiles(profiles = "test")
@EmbeddedKafka(partitions = 1)
public class FullContextITBase {

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

  static int WIREMOCK_PORT;

  @BeforeAll
  static void beforeAll() {
    WireMock.configureFor(WIREMOCK_PORT);
  }

  @DynamicPropertySource
  static void setUpProperties(final DynamicPropertyRegistry registry) {
    Startables.deepStart(POSTGRES_CONTAINER).join();

    bindDatasourceToPostgresOfTestContainers(registry, POSTGRES_CONTAINER);

    WIREMOCK.start();
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(WIREMOCK::isRunning);
    WIREMOCK_PORT = WIREMOCK.port();
    WireMock.configureFor(WIREMOCK_PORT);
    prepareForStart();

    bindUrlsToWiremock(registry, WIREMOCK::baseUrl);

//    bindKafkaBootstrapServersToEmbeddedKafka(registry);
  }

  public static void bindDatasourceToPostgresOfTestContainers(DynamicPropertyRegistry registry,
      PostgreSQLContainer<?> postgresContainer) {
    registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.jdbc-url", postgresContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresContainer::getUsername);
    registry.add("spring.datasource.password", postgresContainer::getPassword);
//    registry.add("postgres.port", () -> postgresContainer::getMappedPort);
    registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
    registry.add("spring.jpa.properties.hibernate.dialect", PostgreSQLDialect.class::getName);
    registry.add("spring.jpa.show-sql", () -> false);
    registry.add("spring.jpa.properties.hibernate.format_sql", () -> false);
    registry.add("spring.jpa.properties.hibernate.use_sql_comments", () -> false);
  }

//  public static void bindKafkaBootstrapServersToEmbeddedKafka(DynamicPropertyRegistry registry) {
//    registry.add("spring.kafka.bootstrap-servers", () -> "${spring.embedded.kafka.brokers}");
//  }

  public static void bindUrlsToWiremock(DynamicPropertyRegistry registry, Supplier<Object> baseUrlSupplier) {
    registry.add("some.integration.url", baseUrlSupplier);
  }

  private static void prepareForStart() {
  // some mocks required at startup
  }

  @Bean
  @Primary
  public ContentFetcherPort contentFetcherPort() {
    return url -> {
      var wordPattern = Pattern.compile("[\\p{L}]{2,}");
      var text = url; // NOSONAR. присваивание переменной для читабельности.
      return wordPattern.matcher(text.toLowerCase())
          .results()
          .map(MatchResult::group)
          .map(String::toLowerCase)
          .collect(Collectors.toSet());
    };
  }

  @AfterEach
  @SuppressWarnings("java:S5786")
  public void commonAfterEach() {
    WireMock.reset();
    prepareForStart();
  }
}