package ru.leonaugust.link_aggregator.postgres.user.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.user.CommunicationChannel;
import ru.leonaugust.link_aggregator.domain.user.User;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication;
import ru.leonaugust.link_aggregator.domain.user.UserCommunication.UserLanguage;
import ru.leonaugust.link_aggregator.domain.user.UserWithLinks;
import ru.leonaugust.link_aggregator.postgres.user.entity.CommunicationChannelDbEnum;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserCommunicationEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserEntity;
import ru.leonaugust.link_aggregator.postgres.user.entity.UserLanguageDbEnum;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  @Mapping(target = "communication", source = "communication")
  UserEntity toEntity(User user);

  default UserCommunicationEntity toCommunicationEntity(UserCommunication communication) {
    return UserCommunicationEntity.builder()
        .email(communication.contacts().get(CommunicationChannel.EMAIL))
        .telegram(communication.contacts().get(CommunicationChannel.TELEGRAM))
        .preferredChannel(toCommunicationChannelDbEnum(communication.preferredChannel()))
        .language(toLanguageDbEnum(communication.language()))
        .build();
  }

  UserLanguageDbEnum toLanguageDbEnum(UserLanguage userLanguage);

  CommunicationChannelDbEnum toCommunicationChannelDbEnum(CommunicationChannel channel);

  @Mapping(target = "communicationDetails", source = "communication")
  UserWithLinks toDtoWithLinks(UserEntity userEntity);

  default UserCommunication toDtoCommunication(UserCommunicationEntity communicationEntity) {
    var contacts = new HashMap<CommunicationChannel, String>();
    if (Objects.nonNull(communicationEntity.getEmail())) {
      contacts.put(CommunicationChannel.EMAIL, communicationEntity.getEmail());
    }
    if (Objects.nonNull(communicationEntity.getTelegram())) {
      contacts.put(CommunicationChannel.TELEGRAM, communicationEntity.getTelegram());
    }
    return UserCommunication.builder()
        .contacts(contacts)
        .language(toLanguage(communicationEntity.getLanguage()))
        .preferredChannel(toCommunicationChannel(communicationEntity.getPreferredChannel()))
        .build();
  }

  UserLanguage toLanguage(UserLanguageDbEnum languageDbEnum);

  CommunicationChannel toCommunicationChannel(CommunicationChannelDbEnum communicationChannelDbEnum);
}