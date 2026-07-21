package ru.leonaugust.link_aggregator.application.impl.user.inbound;

import lombok.RequiredArgsConstructor;
import ru.leonaugust.link_aggregator.application.UseCase;
import ru.leonaugust.link_aggregator.application.api.user.inbound.CreateUserUseCase;
import ru.leonaugust.link_aggregator.application.api.user.outbound.UserRepository;
import ru.leonaugust.link_aggregator.domain.user.User;

@UseCase
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {

  private final UserRepository userRepository;

  @Override
  public void create(User user) {
    user.validate();
    userRepository.save(user);
  }
}
