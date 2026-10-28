package ru.leonaugust.link_aggregator.rest_adapter.link.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkVerifyEvent;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.UserSearchCriteriaDto;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.dto.AddLinkRequestDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LinkV1ControllerMapper {

  @Mapping(target = "key", source = "dto.userSearch", qualifiedByName = "extractKey")
  @Mapping(target = "userSearchCriteria", source = "dto.userSearch")
  @Mapping(target = "link", source = "url")
  LinkVerifyEvent toLinkVerifyEvent(AddLinkRequestDto dto, String url);

  @Mapping(target = "preferredChannel", ignore = true)
  UserSearchCriteria toUserSearchCriteria(UserSearchCriteriaDto criteria);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "securityStatus", ignore = true)
  @Mapping(target = "url", source = "link")
  Link mapLink(String link);

  @Named("extractKey")
  default String extractKey(UserSearchCriteriaDto criteria) {
    return criteria.contacts().get(CommunicationChannel.EMAIL);
  }

}
