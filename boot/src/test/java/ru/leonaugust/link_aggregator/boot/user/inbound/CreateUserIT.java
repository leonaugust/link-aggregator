package ru.leonaugust.link_aggregator.boot.user.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.leonaugust.test_config.FullContextITBase;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.postgres.user.dao.UserRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.user.entity.CommunicationChannelDbEnum;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserCommunicationEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserLanguageDbEnum;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.CommunicationChannelRequestDto;
import ru.leonaugust.link_aggregator.rest_adapter.user.v1.controller.UserV1Controller;
import ru.leonaugust.link_aggregator.rest_adapter.user.v1.dto.CreateUserRequestDto;
import ru.leonaugust.link_aggregator.rest_adapter.user.v1.dto.CreateUserRequestDto.UserLanguageRequestDto;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {MicroserviceStarter.class})
public class CreateUserIT extends FullContextITBase {

  public static final String EMAIL = "test@mail.ru";
  public static final String LINK_FIRST = "link1";
  public static final String LINK_SECOND = "link2";
  public static final String TELEGRAM = "telegram";
  @Value("${server.servlet.context-path}")
  private String rootPath;
  @LocalServerPort
  private int port;

  private WebTestClient webTestClient;
  private final UserRepositoryDao userRepositoryDao;

  @PostConstruct
  void postConstruct() {
    webTestClient = WebTestClient
        .bindToServer().baseUrl("http://localhost:%s/%s".formatted(port, rootPath))
        .responseTimeout(Duration.ofHours(1L))
        .build();
  }

  @AfterEach
  void afterEach() {
    userRepositoryDao.deleteAll();
  }

  @Transactional
  @Test
  void endpointCall_should_saveUser() {
    webTestClient.post()
        .uri(UserV1Controller.PATH)
        .bodyValue(CreateUserRequestDto.builder()
            .contacts(Map.of(
                CommunicationChannel.EMAIL, EMAIL,
                CommunicationChannel.TELEGRAM, TELEGRAM
            ))
            .preferredChannel(CommunicationChannelRequestDto.EMAIL)
            .language(UserLanguageRequestDto.RU)
            .build())
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    var savedUser = userRepositoryDao.findByEmail(EMAIL);
    assertTrue(savedUser.isPresent());
    assertThat(savedUser.get())
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*id", ".*createDate", ".*updateDate", ".*user", ".*links")
        .ignoringCollectionOrder()
        .isEqualTo(
            UserEntity.builder()
                .communication(
                    UserCommunicationEntity.builder()
                        .language(UserLanguageDbEnum.RU)
                        .email(EMAIL)
                        .telegram(TELEGRAM)
                        .preferredChannel(CommunicationChannelDbEnum.EMAIL)
                        .build()
                )
                .build()
        );
  }
}
