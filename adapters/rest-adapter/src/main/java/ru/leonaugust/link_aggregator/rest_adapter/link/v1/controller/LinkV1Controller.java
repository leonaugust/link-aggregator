package ru.leonaugust.link_aggregator.rest_adapter.link.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkVerifyEventProducer;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.dto.AddLinkRequestDto;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.mapper.LinkV1ControllerMapper;
import ru.leonaugust.link_aggregator.util.MeasureTime;

@RestController
@RequestMapping(LinkV1Controller.PATH)
@RequiredArgsConstructor
public class LinkV1Controller {

  public static final String PATH = "/api/v1/link";

  private final LinkVerifyEventProducer linkEventPublisher;
  private final LinkV1ControllerMapper linkV1ControllerMapper;

  @MeasureTime
  @PostMapping
  public void saveLinks(@RequestBody AddLinkRequestDto addLinkRequestDto) {
    addLinkRequestDto.urls().stream()
        .map(url -> linkV1ControllerMapper.toLinkVerifyEvent(addLinkRequestDto, url))
        .forEach(linkEventPublisher::send);
  }

}
