package ru.leonaugust.link_aggregator.domain.link;

/**
 * Результат проверки ссылки на вредоносность внешним сервисом (URLhaus, VirusTotal и т.п.).
 *
 * @param suspicious {@code true}, если ссылка признана опасной
 * @param source     идентификатор источника проверки (например, "urlhaus", "noop")
 * @param detail     детали причины блокировки (может быть {@code null} для безопасного вердикта)
 */
public record LinkSafetyVerdict(boolean suspicious, String source, String detail) {

  public static LinkSafetyVerdict safe(String source) {
    return new LinkSafetyVerdict(false, source, null);
  }

  public static LinkSafetyVerdict violated(String source, String detail) {
    return new LinkSafetyVerdict(true, source, detail);
  }
}