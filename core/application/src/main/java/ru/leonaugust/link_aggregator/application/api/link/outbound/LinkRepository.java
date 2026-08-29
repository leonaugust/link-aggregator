package ru.leonaugust.link_aggregator.application.api.link.outbound;

import java.util.List;
import java.util.UUID;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;

public interface LinkRepository {

  Link save(Link link, UserSearchCriteria userSearchCriteria);

  void update(UUID id, Link link);

  void saveAll(List<Link> links, UserSearchCriteria userSearchCriteria);

  List<Link> getByUserDetails(UserCommunication userCommunication);

}