package ru.leonaugust.link_aggregator.fraud.opentip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ OpenTIP на поиск по URL или домену.
 *
 * <p>Разобран только {@code Zone} — единственное поле верхнего уровня, подтверждённое документацией
 * сервиса и достаточное для вердикта. Имя поля задано явно, потому что глобально включена стратегия
 * именования {@code LOWER_CAMEL_CASE}, а дефолтный ObjectMapper адаптера не наследует настройки
 * контекста Spring.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenTipLookupResponse(@JsonProperty("Zone") String zone) {
}