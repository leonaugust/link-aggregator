package ru.leonaugust.link_aggregator.domain.link;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.leonaugust.link_aggregator.domain.AbstractEvent;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LinkCategorizeEvent extends AbstractEvent {

  private Link link;


}