package ru.leonaugust.link_aggregator.rest_adapter.user.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.leonaugust.link_aggregator.application.api.user.inbound.CreateUserUseCase;
import ru.leonaugust.link_aggregator.rest_adapter.user.v1.dto.CreateUserRequestDto;
import ru.leonaugust.link_aggregator.rest_adapter.user.v1.mapper.CreateUserMapper;

@RestController
@RequestMapping(UserV1Controller.PATH)
@RequiredArgsConstructor
public class UserV1Controller {

  public static final String PATH = "/api/v1/user";

  private final CreateUserUseCase port;
  private final CreateUserMapper mapper;

  @PostMapping
  public void create(@RequestBody CreateUserRequestDto requestDto) {
      port.create(mapper.map(requestDto));
  }

}
