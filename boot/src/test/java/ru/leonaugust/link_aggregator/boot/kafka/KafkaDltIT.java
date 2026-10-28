package ru.leonaugust.link_aggregator.boot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import ru.leonaugust.link_aggregator.boot.MicroserviceStarter;
import ru.leonaugust.link_aggregator.domain.link.Link;
import ru.leonaugust.link_aggregator.domain.link.LinkCategorizeEvent;
import ru.leonaugust.test_config.FullContextITBase;

@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {MicroserviceStarter.class})
public class KafkaDltIT extends FullContextITBase {

  private static final String CATEGORIZE_TOPIC = "link-categorize";
  private static final String CATEGORIZE_DLT_TOPIC = CATEGORIZE_TOPIC + ".DLT";

  private final KafkaTemplate<Object, Object> kafkaTemplate;
  private final EmbeddedKafkaBroker embeddedKafkaBroker;

  @Test
  void categorizeConsumer_unknownLink_failsAfterRetriesAndPublishesToDlt() {
    var linkId = UUID.randomUUID();
    var event = LinkCategorizeEvent.builder()
        .key(linkId.toString())
        .link(Link.builder().id(linkId).url("https://unknown.example.com").build())
        .build();

    kafkaTemplate.send(CATEGORIZE_TOPIC, event.getKey(), event);

    try (var consumer = dltConsumer()) {
      consumer.subscribe(List.of(CATEGORIZE_DLT_TOPIC));
      Awaitility.await()
          .atMost(30, TimeUnit.SECONDS)
          .pollInterval(Duration.ofSeconds(1))
          .untilAsserted(() -> assertThat(poll(consumer))
              .anySatisfy(record -> assertThat(record.key()).isEqualTo(linkId.toString())));
    }
  }

  private KafkaConsumer<String, String> dltConsumer() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt-probe-" + UUID.randomUUID());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    return new KafkaConsumer<>(props);
  }

  private List<ConsumerRecord<String, String>> poll(KafkaConsumer<String, String> consumer) {
    var records = new ArrayList<ConsumerRecord<String, String>>();
    consumer.poll(Duration.ofSeconds(1)).forEach(records::add);
    return records;
  }
}
