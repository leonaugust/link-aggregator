package ru.leonaugust.link_aggregator.postgres.link.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.link.AddLink;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.postgres.link.entity.LinkEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LinkMapper {

  @Mapping(target = "link", source = "link")
  @Mapping(target = "category", source = "category")
  Link toDto(LinkEntity entity);

}
