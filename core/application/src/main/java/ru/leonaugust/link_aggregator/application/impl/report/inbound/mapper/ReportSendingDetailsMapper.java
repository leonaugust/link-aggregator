package ru.leonaugust.link_aggregator.application.impl.report.inbound.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.report.ReportSendingDetails;
import ru.leonaugust.link_aggregator.domain.report.ReportSendingDetails.ReportCategory;

@Mapper(
//    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReportSendingDetailsMapper {

  default ReportSendingDetails toDto(List<Link> links) {
    var categoriesWithLinks = links.stream()
          .collect(Collectors.groupingBy(
              Link::category,
              Collectors.mapping(Link::link, Collectors.toList()
              )
          )).entrySet().stream()
        .map(e -> toReportSendingDetails(e.getKey(), e.getValue()))
        .toList();
    return ReportSendingDetails.builder()
        .categories(categoriesWithLinks)
        .build();
  }

  ReportCategory toReportSendingDetails(String categoryName, List<String> links);

}
