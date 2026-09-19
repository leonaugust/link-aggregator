package ru.leonaugust.link_aggregator.fraud.config;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Настройки антифрод-проверки ссылок.
 *
 * @param baseUrl        базовый URL API выбранного провайдера
 * @param apiKey         API-токен провайдера; для OpenTIP пустой токен недопустим, приложение не стартует
 * @param failOpen       {@code true} — недоступность провайдера считать безопасной ссылкой,
 *                       {@code false} — пробрасывать ошибку и не сохранять ссылку
 * @param requestTimeout суммарный бюджет одной проверки (установка соединения, чтение и разбор ответа),
 *                       его обеспечивает {@code Mono.block(Duration)}; значение по умолчанию задано
 *                       в {@link DefaultValue}, поэтому в конфигурации дублировать его не нужно
 */
@ConditionalOnProperty(name = "link-aggregator.fraud-check.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "link-aggregator.fraud-check")
public record FraudCheckProperties(
    String baseUrl,
    String apiKey,
    boolean failOpen,
    @DefaultValue("10s") Duration requestTimeout) {
}