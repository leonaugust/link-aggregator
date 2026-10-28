package ru.leonaugust.link_aggregator.application.impl.report.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.leonaugust.link_aggregator.application.api.report.outbound.NotificationPort;
import ru.leonaugust.link_aggregator.application.api.user.outbound.UserRepository;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.report.ReportSendingDetails;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.domain.user.UserWithLinks;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SendReportUseCaseImplUTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private NotificationPort notificationPort;

  @Test
  void sendReport_userFound_sendsGroupedReportToPreferredChannel() {
    var criteria = criteria();
    var communication = communication();
    var userWithLinks = new UserWithLinks(communication,
        List.of(Link.builder().url("https://a").category("development").build()));
    when(notificationPort.getNotificationChannel()).thenReturn(CommunicationChannel.EMAIL);
    when(userRepository.getUserWithLinks(criteria)).thenReturn(Optional.of(userWithLinks));
    var useCase = new SendReportUseCaseImpl(List.of(notificationPort), userRepository);
    var detailsCaptor = ArgumentCaptor.forClass(ReportSendingDetails.class);

    useCase.sendReport(criteria);

    verify(notificationPort).sendReport(eq(communication), detailsCaptor.capture());
    assertThat(detailsCaptor.getValue().categories()).hasSize(1);
    assertThat(detailsCaptor.getValue().categories().get(0).categoryName()).isEqualTo("development");
    assertThat(detailsCaptor.getValue().categories().get(0).links()).containsExactly("https://a");
  }

  @Test
  void sendReport_userNotFound_throws() {
    var criteria = criteria();
    when(notificationPort.getNotificationChannel()).thenReturn(CommunicationChannel.EMAIL);
    when(userRepository.getUserWithLinks(criteria)).thenReturn(Optional.empty());
    var useCase = new SendReportUseCaseImpl(List.of(notificationPort), userRepository);

    assertThatThrownBy(() -> useCase.sendReport(criteria))
        .isInstanceOf(NoSuchElementException.class);
  }

  private static UserSearchCriteria criteria() {
    return UserSearchCriteria.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, "test@mail.ru"))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();
  }

  private static UserCommunication communication() {
    return UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, "test@mail.ru"))
        .preferredChannel(CommunicationChannel.EMAIL)
        .language(UserCommunication.UserLanguage.ENG)
        .build();
  }
}
