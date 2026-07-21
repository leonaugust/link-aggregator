package ru.leonaugust.link_aggregator.domain.user;

import java.util.List;
import lombok.With;
import ru.leonaugust.link_aggregator.domain.link.Link;

@With
public record UserWithLinks(
    UserCommunication communicationDetails,
    List<Link> links
) {

}