package ru.leonaugust.link_aggregator.email_adapter.report.outbound;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.leonaugust.link_aggregator.application.api.report.outbound.NotificationPort;
import ru.leonaugust.link_aggregator.domain.report.ReportSendingDetails;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailReportNotification implements NotificationPort {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    String username;
    @Value("${spring.mail.password}")
    String password;

    public void sendReport(UserCommunication userCommunication, ReportSendingDetails report) {
        var email = userCommunication.contacts().get(CommunicationChannel.EMAIL);
        try {
            var context = new Context();
            context.setVariable("report", report);

            var htmlContent = templateEngine.process("report-email-template.html", context);

            // 3. Создаём и отправляем письмо
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("📚 Ваш еженедельный отчёт по закладкам");
            helper.setText(htmlContent, true);   // true = HTML

            mailSender.send(message);
            log.info("Отчёт отправлен пользователю {}", email);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке отчёта для {}", email, e);
            throw new RuntimeException("Не удалось отправить письмо", e);
        }
    }

    @Override
    public CommunicationChannel getNotificationChannel() {
        return CommunicationChannel.EMAIL;
    }
}