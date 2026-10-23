package ru.leonaugust.link_aggregator.kafka.link.categorize.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;
import ru.leonaugust.link_aggregator.application.api.link.inbound.CategorizeLinkUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkRepository;
import ru.leonaugust.link_aggregator.domain.link.ContentPage;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkCategorizeEvent;
import ru.leonaugust.link_aggregator.domain.link.LinkSecurityStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class LinkCategorizeEventConsumerUTest {

  private static final String URL = "https://example.com/";

  @Mock
  private LinkRepository linkRepository;
  @Mock
  private CategorizeLinkUseCase categorizeLinkUseCase;

  @InjectMocks
  private LinkCategorizeEventConsumer consumer;

  @Test
  void consume_eventWithContentPage_passesContentPageToUseCase() {
    var contentPage = new ContentPage("spring boot article text");
    when(categorizeLinkUseCase.categorize(contentPage)).thenReturn(Mono.just("development"));

    consumer.consume(event(link(LinkSecurityStatus.CHECK_OK), contentPage));

    verify(categorizeLinkUseCase).categorize(contentPage);
  }

  @Test
  void consume_eventWithSecurityStatus_keepsStatusOnUpdatedLink() {
    var link = link(LinkSecurityStatus.SUSPICIOUS);
    var contentPage = new ContentPage("text");
    when(categorizeLinkUseCase.categorize(contentPage)).thenReturn(Mono.just("development"));

    consumer.consume(event(link, contentPage));

    var updated = captureUpdatedLink();
    assertThat(updated.id()).isEqualTo(link.id());
    assertThat(updated.securityStatus()).isEqualTo(LinkSecurityStatus.SUSPICIOUS);
    assertThat(updated.category()).isEqualTo("development");
  }

  @Test
  void consume_emptyContentPage_updatesLinkWithCategoryFromUseCase() {
    var contentPage = ContentPage.empty();
    when(categorizeLinkUseCase.categorize(contentPage)).thenReturn(Mono.just("book"));

    consumer.consume(event(link(LinkSecurityStatus.CHECK_OK), contentPage));

    assertThat(captureUpdatedLink().category()).isEqualTo("book");
  }

  @Test
  void consume_nullContentPage_updatesLinkWithoutCategory() {
    when(categorizeLinkUseCase.categorize(null)).thenReturn(Mono.empty());

    consumer.consume(event(link(LinkSecurityStatus.CHECK_OK), null));

    assertThat(captureUpdatedLink().category()).isNull();
  }

  private Link captureUpdatedLink() {
    var captor = ArgumentCaptor.forClass(Link.class);
    verify(linkRepository).update(any(UUID.class), captor.capture());
    return captor.getValue();
  }

  private Link link(LinkSecurityStatus status) {
    return Link.builder()
        .id(UUID.randomUUID())
        .url(URL)
        .securityStatus(status)
        .build();
  }

  private LinkCategorizeEvent event(Link link, ContentPage contentPage) {
    return LinkCategorizeEvent.builder()
        .key(link.id().toString())
        .link(link)
        .contentPage(contentPage)
        .build();
  }
}
