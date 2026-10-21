package ru.leonaugust.link_aggregator.application.api.link.inbound;

import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkVerification;

public interface AntiFraudLinkCheckUseCase {

  LinkVerification check(Link link);

}
