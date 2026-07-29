package ru.leonaugust.link_aggregator.html_fetcher;

import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;

@Component
@Slf4j
public class ContentFetcherPortImpl implements ContentFetcherPort {

  private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}]{2,}"); // только буквы, минимум 2 символа

  @Override
  public Set<String> fetchText(String url) {
    try {
      var doc = Jsoup.connect(url)
          .timeout(5000)
          .userAgent("Mozilla/5.0")
          .followRedirects(true)
          .get();

      String plainText = doc.body().text();

      return WORD_PATTERN.matcher(plainText.toLowerCase())
          .results()
          .map(MatchResult::group)
          .map(String::toLowerCase)
          .collect(Collectors.toSet());

    } catch (Exception e) {
      log.error("Не удалось загрузить или распарсить страницу: {} – {}", url, e.getMessage());
      return Set.of();
    }
  }
}
