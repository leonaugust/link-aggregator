package ru.leonaugust.link_aggregator.application.api.link.outbound;

import ru.leonaugust.link_aggregator.domain.link.LinkVerifyEvent;

public interface LinkVerifyEventProducer {

    void send(LinkVerifyEvent event);

}