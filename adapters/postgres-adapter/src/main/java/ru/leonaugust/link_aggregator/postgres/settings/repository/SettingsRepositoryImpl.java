package ru.leonaugust.link_aggregator.postgres.settings.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.settings.outbound.SettingsRepository;
import ru.leonaugust.link_aggregator.domain.settings.Settings;
import ru.leonaugust.link_aggregator.postgres.settings.dao.SettingsRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.settings.mapper.SettingsMapper;

@Component
@RequiredArgsConstructor
public class SettingsRepositoryImpl implements SettingsRepository {

  private final SettingsRepositoryDao dao;
  private final SettingsMapper mapper;

  @Override
  public void save(Settings settings) {
    dao.save(mapper.toEntity(settings));
  }
}