package ru.leonaugust.link_aggregator.domain.link;

/**
 * Результат антифрод-проверки ссылки: статус безопасности и загруженный контент страницы.
 * <p>
 * Контент передаётся дальше по конвейеру (verify → save → categorize), чтобы страницу
 * не скачивали повторно: тот же текст нужен детектору prompt injection (уже отработал
 * внутри проверки) и категоризатору. Контент в базе не хранится — только в событиях.
 */
public record LinkVerification(LinkSecurityStatus status, ContentPage contentPage) {

  public static LinkVerification ok(ContentPage contentPage) {
    return new LinkVerification(LinkSecurityStatus.CHECK_OK, contentPage);
  }

  public static LinkVerification suspicious() {
    return new LinkVerification(LinkSecurityStatus.SUSPICIOUS, ContentPage.empty());
  }

  public static LinkVerification suspicious(ContentPage contentPage) {
    return new LinkVerification(LinkSecurityStatus.SUSPICIOUS, contentPage);
  }
}
