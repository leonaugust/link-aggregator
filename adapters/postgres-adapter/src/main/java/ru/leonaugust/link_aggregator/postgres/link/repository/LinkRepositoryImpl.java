package ru.leonaugust.link_aggregator.postgres.link.repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkRepository;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.postgres.link.dao.LinkRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.link.entity.LinkEntity;
import ru.leonaugust.link_aggregator.postgres.link.mapper.LinkMapper;
import ru.leonaugust.link_aggregator.postgres.user.dao.UserRepositoryDao;

@Component
@RequiredArgsConstructor
public class LinkRepositoryImpl implements LinkRepository {

  private final LinkRepositoryDao linkRepositoryDao;
  private final UserRepositoryDao userRepositoryDao;
  private final LinkMapper mapper;

  @Override
  public Link save(Link link, UserSearchCriteria userSearchCriteria) {
    var user = userRepositoryDao.findByEmailOrTelegram(userSearchCriteria.getEmail(), userSearchCriteria.getTelegram()).orElseThrow();
    var savedEntity = linkRepositoryDao.save(mapper.toEntity(link, user));
    return mapper.toDto(savedEntity);
  }

  @Override
  public void update(UUID id, Link link) {
    var existing = linkRepositoryDao.findById(id).orElseThrow();
    mapper.merge(existing, link);
    linkRepositoryDao.save(existing);
  }

  @Transactional
  @Override
  public void saveAll(List<Link> links, UserSearchCriteria userSearchCriteria) {
    var user = userRepositoryDao.findByEmailOrTelegram(userSearchCriteria.getEmail(), userSearchCriteria.getTelegram()).orElseThrow();
    if (!CollectionUtils.isEmpty(links)) {
      var entitiesByLink = user.getLinks().stream()
          .collect(Collectors.toMap(LinkEntity::getUrl, Function.identity()));
      var userLinks = user.getLinks();
      links.forEach(link -> {
        var entity = entitiesByLink.get(link.url());
        if (entity == null) {
          var linkEntity = LinkEntity.builder()
              .url(link.url())
              .category(link.category())
              .build();
          linkEntity.setUser(user);
          userLinks.add(linkEntity);
        }
        userRepositoryDao.save(user);
      });
    }
  }

  @Override
  public List<Link> getByUserDetails(UserCommunication userCommunication) {
    var email = userCommunication.contacts().get(CommunicationChannel.EMAIL);
    var telegram = userCommunication.contacts().get(CommunicationChannel.TELEGRAM);
    return linkRepositoryDao.findByEmailOrTelegram(email, telegram)
        .stream()
        .map(mapper::toDto)
        .toList();
  }

}
