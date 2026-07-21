package ru.leonaugust.link_aggregator.postgres.category.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.category.Category;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoryMapper {

  @Mapping(target = "name", source = "name")
  @Mapping(target = "words", source = "words")
  Category toDto(String name, List<String> words);

}
