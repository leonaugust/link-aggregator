package ru.leonaugust.link_aggregator.application.api.link.inbound;

import ru.leonaugust.link_aggregator.domain.link.AddLink;

public interface AddLinkUseCase {

  void process(AddLink addLink);

}