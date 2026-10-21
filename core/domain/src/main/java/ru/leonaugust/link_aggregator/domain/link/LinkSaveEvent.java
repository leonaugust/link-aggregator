package ru.leonaugust.link_aggregator.domain.link;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.leonaugust.link_aggregator.domain.AbstractEvent;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LinkSaveEvent extends AbstractEvent {

  private Link link;
  private UserSearchCriteria userSearchCriteria;
  private ContentPage contentPage;

}
