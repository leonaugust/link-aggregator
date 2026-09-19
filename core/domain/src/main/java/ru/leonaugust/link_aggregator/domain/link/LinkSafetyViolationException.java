package ru.leonaugust.link_aggregator.domain.link;

import lombok.Getter;

/**
 * Исключение, сигнализирующее, что ссылка признана вредоносной
 * по результатам антифрод-проверки и не должна сохраняться.
 */
@Getter
public class LinkSafetyViolationException extends RuntimeException {

  private final String url;
  private final String source;
  private final String detail;

  public LinkSafetyViolationException(String url, String source, String detail) {
    super("Ссылка заблокирована антифрод-проверкой: url=%s, источник=%s, причина=%s"
        .formatted(url, source, detail));
    this.url = url;
    this.source = source;
    this.detail = detail;
  }
}