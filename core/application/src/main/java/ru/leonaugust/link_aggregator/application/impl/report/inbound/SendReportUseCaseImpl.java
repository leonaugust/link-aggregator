package ru.leonaugust.link_aggregator.application.impl.report.inbound;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import ru.leonaugust.link_aggregator.application.UseCase;
import ru.leonaugust.link_aggregator.application.api.report.inbound.SendReportUseCase;
import ru.leonaugust.link_aggregator.application.api.report.outbound.NotificationPort;
import ru.leonaugust.link_aggregator.application.api.user.outbound.UserRepository;
import ru.leonaugust.link_aggregator.application.impl.report.inbound.mapper.ReportSendingDetailsMapper;
import ru.leonaugust.link_aggregator.application.impl.report.inbound.mapper.ReportSendingDetailsMapperImpl;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;

@UseCase
@Slf4j
public class SendReportUseCaseImpl implements SendReportUseCase {

  private static final String SEND_TO_USER_LOG_TEMPLATE = "sending to user: {%s}";

  private final UserRepository userRepository;
  private final ReportSendingDetailsMapper reportSendingDetailsMapper = new ReportSendingDetailsMapperImpl();
  private final Map<CommunicationChannel, NotificationPort> notificationPortByChannel;

  public SendReportUseCaseImpl(List<NotificationPort> notificationPorts,
      UserRepository userRepository) {
    this.notificationPortByChannel = notificationPorts.stream()
        .collect(Collectors.toMap(NotificationPort::getNotificationChannel, Function.identity()));
    this.userRepository = userRepository;
  }


  @Override
  public void sendReport(UserSearchCriteria userSearchCriteria) {
    var user = userRepository.getUserWithLinks(userSearchCriteria).orElseThrow();
    var communicationDetails = user.communicationDetails();
    log.info(SEND_TO_USER_LOG_TEMPLATE.formatted(communicationDetails));
    var notificationPort = notificationPortByChannel.get(communicationDetails.preferredChannel());
    notificationPort.sendReport(communicationDetails, reportSendingDetailsMapper.toDto(user.links()));
  }
}