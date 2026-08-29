package ru.leonaugust.link_aggregator.application.api.link.outbound;

import ru.leonaugust.link_aggregator.domain.link.LinkSaveEvent;

public interface LinkSaveEventProducer {

  void send(LinkSaveEvent event);

}
