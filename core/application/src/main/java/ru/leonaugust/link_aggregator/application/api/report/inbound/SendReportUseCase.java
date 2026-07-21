package ru.leonaugust.link_aggregator.application.api.report.inbound;

import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;

public interface SendReportUseCase {

  void sendReport(UserSearchCriteria userSearchCriteria);

}