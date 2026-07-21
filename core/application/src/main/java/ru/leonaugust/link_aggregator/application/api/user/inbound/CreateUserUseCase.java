package ru.leonaugust.link_aggregator.application.api.user.inbound;

import ru.leonaugust.link_aggregator.domain.user.User;

public interface CreateUserUseCase {

  void create(User createUser);

}
