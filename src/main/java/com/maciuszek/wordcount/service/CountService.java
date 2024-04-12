package com.maciuszek.wordcount.service;

import reactor.core.publisher.Flux;

public interface CountService<T, E>  {

    T count(E s);

    default Flux<String> scrapeWords(String stringOfWords) {
        return Flux.fromArray(
                format(stringOfWords)
                        .split("[^\\w']+") // filter alphanumeric words
        );
    }

    default String format(String stringOfWords) {
        return stringOfWords.toLowerCase(); // assume word count shouldn't be case-sensitive
    }

}
