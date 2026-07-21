package ru.leonaugust.link_aggregator.rest_adapter.link.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.leonaugust.link_aggregator.application.api.link.inbound.AddLinkUseCase;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.dto.AddLinkRequestDto;
import ru.leonaugust.link_aggregator.rest_adapter.link.v1.mapper.AddLinkMapper;

@RestController
@RequestMapping(LinkV1Controller.PATH)
@RequiredArgsConstructor
public class LinkV1Controller {

  public static final String PATH = "/api/v1/link";

  private final AddLinkUseCase addLinkPort;
  private final AddLinkMapper addLinkMapper;

  @PostMapping
  public void saveLinks(@RequestBody AddLinkRequestDto addLinkRequestDto) {
      addLinkPort.process(addLinkMapper.map(addLinkRequestDto));
  }

}
