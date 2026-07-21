package ru.leonaugust.link_aggregator.application.api.link.outbound;

import java.util.List;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;

public interface LinkRepository {

  void saveAll(List<Link> links, UserSearchCriteria userSearchCriteria);

  List<Link> getByUserDetails(UserCommunication userCommunication);

}