package ru.leonaugust.link_aggregator.postgres.user.repository;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.user.outbound.UserRepository;
import ru.leonaugust.link_aggregator.domain.user.User;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.domain.user.UserWithLinks;
import ru.leonaugust.link_aggregator.postgres.user.dao.UserRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.user.mapper.UserMapper;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final UserRepositoryDao dao;
  private final UserMapper mapper;

  @Transactional
  @Override
  public Optional<UserWithLinks> getUserWithLinks(UserSearchCriteria userSearchCriteria) {
    return dao.findByEmailOrTelegram(userSearchCriteria.getEmail(), userSearchCriteria.getTelegram())
        .map(mapper::toDtoWithLinks);
  }

  @Override
  public void save(User user) {
    dao.save(mapper.toEntity(user));
  }
}