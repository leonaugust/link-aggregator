package ru.leonaugust.link_aggregator.postgres.user.dao;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.leonaugust.link_aggregator.domain.user.User;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;

@Repository
public interface UserRepositoryDao extends JpaRepository<UserEntity, UUID> {

  @Query("SELECT u FROM UserEntity u JOIN u.communication c WHERE c.email = :email")
  Optional<UserEntity> findByEmail(@Param("email") String email);

  @Query("SELECT u FROM UserEntity u JOIN u.communication c " +
      "WHERE (:email IS NOT NULL AND c.email = :email) " +
      "OR (:telegram IS NOT NULL AND c.telegram = :telegram)")
  Optional<UserEntity> findByEmailOrTelegram(@Param("email") String email,
      @Param("telegram") String telegram);

}
