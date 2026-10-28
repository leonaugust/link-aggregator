package ru.leonaugust.link_aggregator.postgres.link.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.postgres.link.entity.LinkEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LinkMapper {

  @Mapping(target = "url", source = "url")
  @Mapping(target = "category", source = "category")
  Link toDto(LinkEntity entity);

  @Mapping(target = "user", ignore = true)
  @Mapping(target = "url", source = "link.url")
  @Mapping(target = "category", source = "link.category")
  void merge(@MappingTarget LinkEntity entity, Link link);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "url", ignore = true)
  @Mapping(target = "category", ignore = true)
  void mergeSecurityStatus(@MappingTarget LinkEntity entity, Link link);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", source = "user")
  @Mapping(target = "url", source = "link.url")
  @Mapping(target = "category", source = "link.category")
  LinkEntity toEntity(Link link, UserEntity user);

}
