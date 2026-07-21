package ru.leonaugust.link_aggregator.postgres.category.dao;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.leonaugust.link_aggregator.postgres.category.entity.CategoryEntity;

@Repository
public interface CategoryRepositoryDao extends JpaRepository<CategoryEntity, UUID> {

  boolean existsByNameAndWord(String name, String word);

  boolean existsByName(String name);

}
