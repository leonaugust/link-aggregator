package ru.leonaugust.link_aggregator.domain.report;

import java.util.List;
import lombok.Builder;

@Builder
public record ReportSendingDetails(
    List<ReportCategory> categories
) {

  public record ReportCategory(String categoryName, List<String> links) {

  }

}
