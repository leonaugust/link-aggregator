package ru.leonaugust.link_aggregator.domain.link;

/**
 * Текст страницы, полученный на этапе антифрод-проверки.
 * <p>
 * Загружается один раз и передаётся дальше по конвейеру (verify → save → categorize),
 * чтобы страницу не скачивали повторно: тот же текст нужен и детектору
 * prompt injection, и категоризатору. Пустая страница или {@code null} допустимы —
 * это означает, что контент получить не удалось.
 */
public record ContentPage(String text) {

  public static ContentPage empty() {
    return new ContentPage("");
  }

  public boolean isEmpty() {
    return text == null || text.isBlank();
  }
}
