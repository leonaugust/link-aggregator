package ru.leonaugust.link_aggregator.application.impl.link.inbound;

import lombok.RequiredArgsConstructor;
import ru.leonaugust.link_aggregator.application.UseCase;
import ru.leonaugust.link_aggregator.application.api.link.inbound.AntiFraudLinkCheckUseCase;
import ru.leonaugust.link_aggregator.domain.link.Link;

@UseCase
@RequiredArgsConstructor
public class AntiFraudLinkCheckUseCaseImpl implements AntiFraudLinkCheckUseCase {

  @Override
  public void check(Link link) {
    // add check for virus total or throw Exception
  }
}
