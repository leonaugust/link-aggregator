package ru.leonaugust.link_aggregator.boot.report;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import ru.leonaugust.link_aggregator.application.impl.report.inbound.SendReportUseCaseImpl;
import ru.leonaugust.link_aggregator.boot.FullContextITBase;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.postgres.link.dao.LinkRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.link.entity.LinkEntity;
import ru.leonaugust.link_aggregator.postgres.user.dao.UserRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.user.entity.CommunicationChannelDbEnum;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserCommunicationEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserLanguageDbEnum;

@Disabled
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = MicroserviceStarter.class)
public class ReportSendingE2EIT extends FullContextITBase {

  @Value("${MAIL_SENDER_RECIPIENT:recipient}")
  private String email;

  private static final String DEVELOPMENT_LINK = "https://habr.com/ru/companies/skillfactory/articles/811769/";
  private static final String BOOK_LINK = "https://author.today/contest/41";

  private final SendReportUseCaseImpl reportSendingPort;
  private final LinkRepositoryDao linkRepositoryDao;
  private final UserRepositoryDao userRepositoryDao;

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
            .email(email)
            .build()
    );
    userRepositoryDao.save(userEntity);

    linkRepositoryDao.saveAllAndFlush(
        List.of(
            LinkEntity.builder()
                .user(userEntity)
                .link(DEVELOPMENT_LINK)
                .category("development")
                .build(),
            LinkEntity.builder()
                .user(userEntity)
                .link(BOOK_LINK)
                .category("book")
                .build()
        )
    );

    reportSendingPort.sendReport(UserSearchCriteria.builder()
        .contacts(Map.of(
            CommunicationChannel.EMAIL, email
        ))
        .build());

  }


}
