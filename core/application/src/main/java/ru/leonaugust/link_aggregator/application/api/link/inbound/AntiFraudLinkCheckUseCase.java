package ru.leonaugust.link_aggregator.application.api.link.inbound;

import ru.leonaugust.link_aggregator.domain.link.AddLink;
import ru.leonaugust.link_aggregator.domain.link.Link;

public interface AntiFraudLinkCheckUseCase {

  void check(Link link);

}
