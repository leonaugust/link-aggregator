package ru.leonaugust.link_aggregator.postgres.settings.dao;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.leonaugust.link_aggregator.postgres.settings.entity.SettingsEntity;

@Repository
public interface SettingsRepositoryDao extends JpaRepository<SettingsEntity, UUID> {

  boolean existsByName(String name);

}
