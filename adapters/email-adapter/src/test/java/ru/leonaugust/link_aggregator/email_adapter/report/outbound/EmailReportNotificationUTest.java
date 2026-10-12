package ru.leonaugust.link_aggregator.email_adapter.report.outbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.leonaugust.link_aggregator.domain.report.ReportSendingDetails;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class EmailReportNotificationUTest {

  private static final String EMAIL = "test@mail.ru";

  @Mock
  private JavaMailSender mailSender;
  @Mock
  private SpringTemplateEngine templateEngine;

  @InjectMocks
  private EmailReportNotification notification;

  @Test
  void getNotificationChannel_returnsEmail() {
    assertThat(notification.getNotificationChannel()).isEqualTo(CommunicationChannel.EMAIL);
  }

  @Test
  void sendReport_emailContact_sendsHtmlMessage() throws Exception {
    var message = new MimeMessage(Session.getInstance(new Properties()));
    when(mailSender.createMimeMessage()).thenReturn(message);
    when(templateEngine.process(eq("report-email-template.html"), any(IContext.class)))
        .thenReturn("<html>report</html>");
    var communication = UserCommunication.builder()
        .contacts(Map.of(CommunicationChannel.EMAIL, EMAIL))
        .preferredChannel(CommunicationChannel.EMAIL)
        .build();
    var report = ReportSendingDetails.builder()
        .categories(List.of(new ReportSendingDetails.ReportCategory("development", List.of("https://a"))))
        .build();

    notification.sendReport(communication, report);

    verify(mailSender).send(message);
    assertThat(message.getAllRecipients()[0].toString()).isEqualTo(EMAIL);
  }
}
