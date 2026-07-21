package ru.leonaugust.link_aggregator.application.api.report.outbound;

import ru.leonaugust.link_aggregator.domain.report.ReportSendingDetails;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;

public interface NotificationPort {

  void sendReport(UserCommunication userCommunication, ReportSendingDetails report);

  CommunicationChannel getNotificationChannel();

}
