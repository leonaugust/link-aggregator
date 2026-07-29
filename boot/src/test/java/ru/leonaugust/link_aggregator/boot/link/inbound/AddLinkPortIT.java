package ru.leonaugust.link_aggregator.boot.link.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
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
import ru.leonaugust.link_aggregator.boot.FullContextITBase;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.postgres.link.dao.LinkRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.link.entity.LinkEntity;
import ru.leonaugust.link_aggregator.postgres.user.dao.UserRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.user.entity.CommunicationChannelDbEnum;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserCommunicationEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserLanguageDbEnum;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.UserSearchCriteriaDto;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.controller.LinkV1Controller;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.dto.AddLinkRequestDto;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MicroserviceStarter.class)
public class AddLinkPortIT extends FullContextITBase {

  public static final String EMAIL = "test@mail.ru";
  public static final String LINK_FIRST = "link1";
  public static final String LINK_SECOND = "link2";
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

  @AfterEach
  void afterEach() {
    userRepositoryDao.deleteAll();
  }

  @Test
  void endpointCall_should_saveLinks() {
    var userEntity = UserEntity.builder()
        .build();
    userEntity = userRepositoryDao.save(userEntity);
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
                .contact(Map.of(CommunicationChannel.EMAIL, EMAIL))
                .build())
            .links(List.of(LINK_FIRST, LINK_SECOND))
            .build())
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    var savedLinks = linkRepositoryDao.findByEmailOrTelegram(EMAIL, null);
    assertThat(savedLinks.size()).isEqualTo(2);
    assertThat(savedLinks)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .ignoringExpectedNullFields()
        .ignoringCollectionOrder()
        .isEqualTo(List.of(
            LinkEntity.builder()
                .link(LINK_FIRST)
                .category("undefined")
                .build(),
            LinkEntity.builder()
                .link(LINK_SECOND)
                .category("undefined")
                .build()
        ));
  }

}
