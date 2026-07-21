package ru.leonaugust.link_aggregator.rest_adapter.user.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.user.User;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.rest_adapter.user.v1.dto.CreateUserRequestDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CreateUserMapper {

  @Mapping(target = "communication", source = "dto")
  User map(CreateUserRequestDto dto);

  UserCommunication toUserCommunication(CreateUserRequestDto dto);

}
