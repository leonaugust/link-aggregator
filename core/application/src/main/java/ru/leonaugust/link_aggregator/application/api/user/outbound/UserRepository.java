package ru.leonaugust.link_aggregator.application.api.user.outbound;

import java.util.Optional;
import ru.leonaugust.link_aggregator.domain.user.User;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.domain.user.UserWithLinks;

public interface UserRepository {

  Optional<UserWithLinks> getUserWithLinks(UserSearchCriteria userSearchCriteria);

  void save(User createUser);

}
