package ru.leonaugust.link_aggregator.kafka.link.save.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkCategorizeEventProducer;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkRepository;
import ru.leonaugust.link_aggregator.domain.link.ContentPage;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkCategorizeEvent;
import ru.leonaugust.link_aggregator.domain.link.LinkSaveEvent;
import ru.leonaugust.link_aggregator.domain.link.LinkSecurityStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class LinkSaveEventConsumerUTest {

  private static final String URL = "https://example.com/";

  @Mock
  private LinkRepository linkRepository;
  @Mock
  private LinkCategorizeEventProducer linkCategorizeEventProducer;

  @InjectMocks
  private LinkSaveEventConsumer consumer;

  @Test
  void consume_suspiciousLink_savesLinkAndSkipsCategorization() {
    var savedLink = savedLink(LinkSecurityStatus.SUSPICIOUS);
    when(linkRepository.save(any(), any())).thenReturn(savedLink);

    consumer.consume(saveEvent(savedLink, new ContentPage("malicious text")));

    verify(linkRepository).save(savedLink, null);
    verify(linkCategorizeEventProducer, never()).send(any());
  }

  @Test
  void consume_suspiciousEventWithCheckOkPersistedRow_skipsCategorization() {
    var persistedLink = savedLink(LinkSecurityStatus.CHECK_OK);
    when(linkRepository.save(any(), any())).thenReturn(persistedLink);

    consumer.consume(saveEvent(savedLink(LinkSecurityStatus.SUSPICIOUS), ContentPage.empty()));

    verify(linkCategorizeEventProducer, never()).send(any());
  }

  @Test
  void consume_checkOkLink_sendsCategorizeEventWithContentPage() {
    var savedLink = savedLink(LinkSecurityStatus.CHECK_OK);
    var contentPage = new ContentPage("spring boot article text");
    when(linkRepository.save(any(), any())).thenReturn(savedLink);

    consumer.consume(saveEvent(savedLink, contentPage));

    var captor = ArgumentCaptor.forClass(LinkCategorizeEvent.class);
    verify(linkCategorizeEventProducer).send(captor.capture());
    assertThat(captor.getValue().getContentPage()).isEqualTo(contentPage);
    assertThat(captor.getValue().getKey()).isEqualTo(savedLink.id().toString());
    assertThat(captor.getValue().getLink()).isEqualTo(savedLink);
  }

  @Test
  void consume_savedLinkWithoutStatus_sendsCategorizeEvent() {
    var savedLink = savedLink(null);
    when(linkRepository.save(any(), any())).thenReturn(savedLink);

    consumer.consume(saveEvent(savedLink, ContentPage.empty()));

    verify(linkCategorizeEventProducer).send(any());
  }

  private Link savedLink(LinkSecurityStatus status) {
    return Link.builder()
        .id(UUID.randomUUID())
        .url(URL)
        .securityStatus(status)
        .build();
  }

  private LinkSaveEvent saveEvent(Link link, ContentPage contentPage) {
    return LinkSaveEvent.builder()
        .key(link.id().toString())
        .link(link)
        .contentPage(contentPage)
        .build();
  }
}
