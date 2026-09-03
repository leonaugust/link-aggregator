package ru.leonaugust.link_aggregator.boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.leonaugust.link_aggregator.application.api.link.outbound.ContentFetcherPort;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
public class ContentFetcherConfig {

    @Bean
    @Primary
    public ContentFetcherPort contentFetcherPort() {
        return url -> {
            var wordPattern = Pattern.compile("[\\p{L}]{2,}");
            var text = url; // NOSONAR. присваивание переменной для читабельности.
            return wordPattern.matcher(text.toLowerCase())
                    .results()
                    .map(MatchResult::group)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        };
    }
}
