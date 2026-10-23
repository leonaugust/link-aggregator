package ru.leonaugust.link_aggregator.html_fetcher;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;
import ru.leonaugust.link_aggregator.domain.link.ContentPage;

@Component
@Slf4j
public class ContentFetcherPortImpl implements ContentFetcherPort {

  // Kafka max.request.size по умолчанию 1 MB, JSON-экранирование текста способно расширить его в 6 раз
  public static final int MAX_CONTENT_LENGTH = 128 * 1024;

  @Override
  public ContentPage fetch(String url) {
    try {
      var doc = Jsoup.connect(url)
          .timeout(5000)
          .userAgent("Mozilla/5.0")
          .followRedirects(true)
          .get();

      return new ContentPage(truncate(doc.body().text()));
    } catch (Exception e) {
      log.error("Не удалось загрузить или распарсить страницу: {} – {}", url, e.getMessage());
      return ContentPage.empty();
    }
  }

  static String truncate(String text) {
    if (text.length() <= MAX_CONTENT_LENGTH) {
      return text;
    }
    log.warn("Текст страницы длиннее {} символов обрезан", MAX_CONTENT_LENGTH);
    return text.substring(0, MAX_CONTENT_LENGTH);
  }
}
