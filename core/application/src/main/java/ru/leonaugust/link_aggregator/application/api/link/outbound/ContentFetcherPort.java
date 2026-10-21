package ru.leonaugust.link_aggregator.application.api.link.outbound;

import ru.leonaugust.link_aggregator.domain.link.ContentPage;

public interface ContentFetcherPort {

  ContentPage fetch(String url);
}
