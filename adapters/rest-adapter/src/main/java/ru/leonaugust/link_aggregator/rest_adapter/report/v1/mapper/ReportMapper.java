package ru.leonaugust.link_aggregator.rest_adapter.report.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;
import ru.leonaugust.link_aggregator.rest_adapter.common.dto.UserSearchCriteriaDto;
import ru.leonaugust.link_aggregator.rest_adapter.report.v1.dto.SendReportRequestDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReportMapper {

  @Mapping(target = "preferredChannel", ignore = true)
  @Mapping(target = "contacts", source = "dto.userSearchRequestDto.contact")
  UserSearchCriteria map(SendReportRequestDto dto);

  @Mapping(target = "preferredChannel", ignore = true)
  @Mapping(target = "contacts", source = "contact")
  UserSearchCriteria mapUserCommunication(UserSearchCriteriaDto userCommunicationRequestDto);

}
