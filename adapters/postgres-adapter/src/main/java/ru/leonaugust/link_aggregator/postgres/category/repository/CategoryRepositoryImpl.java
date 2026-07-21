package ru.leonaugust.link_aggregator.postgres.category.repository;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.category.outbound.CategoryRepository;
import ru.leonaugust.link_aggregator.domain.category.Category;
import ru.leonaugust.link_aggregator.postgres.category.dao.CategoryRepositoryDao;
import ru.leonaugust.link_aggregator.postgres.category.entity.CategoryEntity;
import ru.leonaugust.link_aggregator.postgres.category.mapper.CategoryMapper;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

  private final CategoryRepositoryDao dao;
  private final CategoryMapper mapper;

  // TODO: enableCaching
  @Override
  public List<Category> getCategories() {
    return dao.findAll()
        .stream()
        .collect(groupingBy(
            CategoryEntity::getName,
            mapping(CategoryEntity::getWord, toList())
        ))
        .entrySet()
        .stream()
        .map(entry -> mapper.toDto(entry.getKey(), entry.getValue()))
        .collect(toList());
  }

}
