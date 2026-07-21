package ru.leonaugust.link_aggregator.rest_adapter.link.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.link.AddLink;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.UserSearchCriteriaDto;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.dto.AddLinkRequestDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AddLinkMapper {

  @Mapping(target = "userSearchCriteria", source = "userSearch")
  @Mapping(target = "links", source = "links")
  AddLink map(AddLinkRequestDto addLinkRequestDto);

  @Mapping(target = "preferredChannel", ignore = true)
  @Mapping(target = "contacts", source = "contact")
  UserSearchCriteria toUserSearchCriteria(UserSearchCriteriaDto criteria);

  @Mapping(target = "category", ignore = true)
  @Mapping(target = "link", source = "link")
  Link mapLink(String link);

}
