package ru.leonaugust.link_aggregator.postgres.settings.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.settings.Settings;
import ru.leonaugust.link_aggregator.postgres.settings.entity.SettingsEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SettingsMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "name", source = "name")
  SettingsEntity toEntity(Settings settings);

}
