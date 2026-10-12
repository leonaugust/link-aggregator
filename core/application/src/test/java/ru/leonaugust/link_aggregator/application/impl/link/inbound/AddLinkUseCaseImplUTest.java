package ru.leonaugust.link_aggregator.application.impl.link.inbound;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.application.api.link.inbound.CategorizeLinkUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkRepository;
import ru.leonaugust.link_aggregator.domain.link.AddLink;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.user.UserSearchCriteria;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class AddLinkUseCaseImplUTest {

  @Mock
  private LinkRepository linkRepository;
  @Mock
  private CategorizeLinkUseCase categorizeLinkUseCase;

  @InjectMocks
  private AddLinkUseCaseImpl useCase;

  @Test
  void process_linksProvided_savesLinksWithResolvedCategories() {
    var link = Link.builder().url("https://example.com").build();
    var criteria = UserSearchCriteria.builder().build();
    when(categorizeLinkUseCase.categorize("https://example.com")).thenReturn(Mono.just("development"));
    var addLink = AddLink.builder().links(List.of(link)).userSearchCriteria(criteria).build();

    useCase.process(addLink);

    verify(linkRepository).saveAll(List.of(link.withCategory("development")), criteria);
  }

  @Test
  void process_emptyLinks_savesEmptyList() {
    var criteria = UserSearchCriteria.builder().build();
    var addLink = AddLink.builder().links(List.of()).userSearchCriteria(criteria).build();

    useCase.process(addLink);

    verify(linkRepository).saveAll(List.of(), criteria);
  }
}
