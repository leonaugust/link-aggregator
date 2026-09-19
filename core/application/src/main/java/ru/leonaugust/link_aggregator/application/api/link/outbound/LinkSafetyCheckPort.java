package ru.leonaugust.link_aggregator.application.api.link.outbound;

import ru.leonaugust.link_aggregator.domain.link.LinkSafetyVerdict;

/**
 * Исходящий порт для проверки ссылки на вредоносность внешним сервисом.
 * Реализации: URLhaus-адаптер, NoOp-заглушка и т.п.
 */
public interface LinkSafetyCheckPort {

  LinkSafetyVerdict check(String url);

}