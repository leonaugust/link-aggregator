package ru.leonaugust.link_aggregator.domain.link;

import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;

@Builder
@Jacksonized
public record AddLink(
    List<Link> links,
    UserSearchCriteria userSearchCriteria
) {


}