package ru.leonaugust.link_aggregator.postgres.link.dao;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.leonaugust.link_aggregator.postgres.link.entity.LinkEntity;

@Repository
public interface LinkRepositoryDao extends JpaRepository<LinkEntity, UUID> {

  @Query("SELECT l FROM LinkEntity l " +
      "JOIN l.user u " +
      "JOIN u.communication c " +
      "WHERE (:email IS NULL OR c.email = :email) " +
      "AND (:telegram IS NULL OR c.telegram = :telegram)")
  List<LinkEntity> findByEmailOrTelegram(@Param("email") String email,
      @Param("telegram") String telegram);
}
