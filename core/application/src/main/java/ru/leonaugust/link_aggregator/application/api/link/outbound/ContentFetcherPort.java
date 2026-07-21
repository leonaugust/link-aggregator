package ru.leonaugust.link_aggregator.application.api.link.outbound;

import java.util.Set;

public interface ContentFetcherPort {

  Set<String> fetchText(String url);

}
