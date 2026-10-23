package ru.leonaugust.link_aggregator.kafka.link.verify.consumer;

import static org.assertj.core.api.Assertions.assertThat;
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
import ru.leonaugust.link_aggregator.application.api.link.inbound.AntiFraudLinkCheckUseCase;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSaveEventProducer;
import ru.leonaugust.link_aggregator.domain.link.ContentPage;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkSaveEvent;
import ru.leonaugust.link_aggregator.domain.link.LinkSecurityStatus;
import ru.leonaugust.link_aggregator.domain.link.LinkVerification;
import ru.leonaugust.link_aggregator.domain.link.LinkVerifyEvent;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class LinkVerifyEventConsumerUTest {

  private static final String URL = "https://example.com/";

  @Mock
  private AntiFraudLinkCheckUseCase antiFraudLinkCheckUseCase;
  @Mock
  private LinkSaveEventProducer linkSaveEventProducer;

  @InjectMocks
  private LinkVerifyEventConsumer consumer;

  @Test
  void consume_suspiciousVerification_sendsSaveEventWithSuspiciousStatusAndEmptyContent() {
    var link = Link.builder().id(UUID.randomUUID()).url(URL).build();
    when(antiFraudLinkCheckUseCase.check(link))
        .thenReturn(LinkVerification.suspicious());

    consumer.consume(LinkVerifyEvent.builder().key("key").link(link).build());

    var saveEvent = captureSaveEvent();
    assertThat(saveEvent.getLink().securityStatus()).isEqualTo(LinkSecurityStatus.SUSPICIOUS);
    assertThat(saveEvent.getContentPage()).isEqualTo(ContentPage.empty());
  }

  @Test
  void consume_checkOkVerification_sendsSaveEventWithCheckOkStatusAndContent() {
    var link = Link.builder().id(UUID.randomUUID()).url(URL).build();
    var contentPage = new ContentPage("spring boot article text");
    when(antiFraudLinkCheckUseCase.check(link))
        .thenReturn(LinkVerification.ok(contentPage));

    consumer.consume(LinkVerifyEvent.builder().key("key").link(link).build());

    var saveEvent = captureSaveEvent();
    assertThat(saveEvent.getLink().securityStatus()).isEqualTo(LinkSecurityStatus.CHECK_OK);
    assertThat(saveEvent.getContentPage()).isEqualTo(contentPage);
  }

  @Test
  void consume_eventWithKey_propagatesKey() {
    var link = Link.builder().id(UUID.randomUUID()).url(URL).build();
    when(antiFraudLinkCheckUseCase.check(link))
        .thenReturn(LinkVerification.ok(ContentPage.empty()));

    consumer.consume(LinkVerifyEvent.builder().key("user-id").link(link).build());

    assertThat(captureSaveEvent().getKey()).isEqualTo("user-id");
  }

  private LinkSaveEvent captureSaveEvent() {
    var captor = ArgumentCaptor.forClass(LinkSaveEvent.class);
    verify(linkSaveEventProducer).send(captor.capture());
    return captor.getValue();
  }
}
