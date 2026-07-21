package ru.leonaugust.link_aggregator.application.api.settings.outbound;

import ru.leonaugust.link_aggregator.domain.settings.Settings;

public interface SettingsRepository {

  void save(Settings settings);

}
