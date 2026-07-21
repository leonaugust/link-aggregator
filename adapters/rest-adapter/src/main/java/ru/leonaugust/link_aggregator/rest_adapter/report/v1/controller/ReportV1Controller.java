package ru.leonaugust.link_aggregator.rest_adapter.report.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.leonaugust.link_aggregator.application.api.report.inbound.SendReportUseCase;
import ru.leonaugust.link_aggregator.rest_adapter.report.v1.dto.SendReportRequestDto;
import ru.leonaugust.link_aggregator.rest_adapter.report.v1.mapper.ReportMapper;

@RestController
@RequestMapping(ReportV1Controller.PATH)
@RequiredArgsConstructor
public class ReportV1Controller {

  public static final String PATH = "/api/v1/report";

  private final SendReportUseCase port;
  private final ReportMapper mapper;

  @PostMapping
  public void create(@RequestBody SendReportRequestDto requestDto) {
      port.sendReport(mapper.map(requestDto));
  }

}
