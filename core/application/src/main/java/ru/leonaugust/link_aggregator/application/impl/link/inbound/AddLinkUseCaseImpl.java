package ru.leonaugust.link_aggregator.application.impl.link.inbound;

import lombok.RequiredArgsConstructor;
import ru.leonaugust.link_aggregator.application.UseCase;
import ru.leonaugust.link_aggregator.application.api.link.inbound.AddLinkUseCase;
import ru.leonaugust.link_aggregator.application.api.link.inbound.CategorizeLinkUseCase;
import ru.leonaugust.link_aggregator.domain.link.AddLink;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkRepository;

@UseCase
@RequiredArgsConstructor
public class AddLinkUseCaseImpl implements AddLinkUseCase {

  private final LinkRepository linkRepository;
  private final CategorizeLinkUseCase categorizeLinkUseCase;

  @Override
  public void process(AddLink addLink) {
    var links = addLink.links().stream()
        .map(link -> link.withCategory(categorizeLinkUseCase.categorize(link.url()).block()))
        .toList();
    linkRepository.saveAll(links, addLink.userSearchCriteria());
  }
}
