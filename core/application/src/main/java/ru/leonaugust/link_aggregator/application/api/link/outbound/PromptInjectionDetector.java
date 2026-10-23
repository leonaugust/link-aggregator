package ru.leonaugust.link_aggregator.application.api.link.outbound;

import reactor.core.publisher.Mono;

/**
 * Детектор prompt-injection атак в тексте.
 * Реализации не должны бросать ошибки: при невозможности выполнить проверку
 * (например, недоступен embed-сервис) возвращается false (fail-open),
 * чтобы сбой детектора не блокировал основной поток.
 */
public interface PromptInjectionDetector {

    Mono<Boolean> isInjection(String text);
}
