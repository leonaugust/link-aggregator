package ru.leonaugust.link_aggregator.fraud.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Конфигурация антифрод-проверки через Kaspersky OpenTIP.
 */
@Configuration
@ConditionalOnProperty(name = "link-aggregator.fraud-check.enabled", havingValue = "true")
@ConditionalOnProperty(name = "link-aggregator.fraud-check.provider", havingValue = "opentip")
@EnableConfigurationProperties(FraudCheckProperties.class)
public class OpenTipConfig {

  @Bean
  @Qualifier("openTipWebClient")
  public WebClient openTipWebClient(FraudCheckProperties props) {
    if (props.apiKey() == null || props.apiKey().isBlank()) {
      throw new IllegalStateException(
          "Пустой API-токен Kaspersky OpenTIP: задайте переменную окружения OPENTIP_API_KEY "
              + "или свойство link-aggregator.fraud-check.api-key. "
              + "Без токена OpenTIP отвечает 401 на каждый запрос и антифрод-проверка отключена. "
              + "Токен выдаётся на https://opentip.kaspersky.com/");
    }
    return WebClient.builder()
        .baseUrl(props.baseUrl())
        .build();
  }
}