package ru.leonaugust.link_aggregator.fraud.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.leonaugust.link_aggregator.application.api.link.outbound.LinkSafetyCheckPort;
import ru.leonaugust.link_aggregator.domain.link.LinkSafetyVerdict;

/**
 * Fallback-реализация порта проверки ссылок, активная по умолчанию (matchIfMissing).
 * Используется, когда антифрод-проверка отключена или стратегия не выбрана, чтобы
 * конструктор {@code AntiFraudLinkCheckUseCaseImpl} всегда мог получить бин порта.
 */
@Configuration
public class FraudCheckFallbackConfig {

  @Bean
  @ConditionalOnProperty(name = "link-aggregator.fraud-check.provider", havingValue = "noop", matchIfMissing = true)
  public LinkSafetyCheckPort noOpLinkSafetyCheckPort() {
    return url -> LinkSafetyVerdict.safe("noop");
  }
}