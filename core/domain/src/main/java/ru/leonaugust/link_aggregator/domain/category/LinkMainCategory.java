package ru.leonaugust.link_aggregator.domain.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LinkMainCategory {
    DEVELOPMENT("development"),
    BOOK("book"),
    MOVIE("movie");

    final String description;


}
