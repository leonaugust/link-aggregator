package ru.leonaugust.link_aggregator.application.api.link.outbound;

import ru.leonaugust.link_aggregator.domain.link.LinkCategorizeEvent;

public interface LinkCategorizeEventProducer {

    void send(LinkCategorizeEvent event);

}