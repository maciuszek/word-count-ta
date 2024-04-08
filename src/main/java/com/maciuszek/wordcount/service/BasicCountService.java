package com.maciuszek.wordcount.service;

import com.maciuszek.wordcount.domain.WordCount;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class BasicCountService implements CountService<Flux<WordCount>, Flux<String>> {

    public Flux<WordCount> count(Flux<String> flux) {
        // count words using hash key collision in hashmap
        Map<String, Integer> wordCountMap = new HashMap<>();

        return flux.flatMap(this::scrapeWords)
                .doOnNext(word -> wordCountMap.compute(
                        word,
                        (key, val) -> (val == null) ? 1 : val + 1
                )) // since this implies potentially infinite vertical growth of memory, for production instead of one thenMany, we would probably want to flush the hashmap periodically to a key-value database (one that supports reactive connectivity and can be scaled horizontally) then consolidate as a reactive stream from the database
                .thenMany(Flux.fromStream(() ->
                    wordCountMap.entrySet().stream()
                            .map(entrySet -> new WordCount(entrySet.getKey(), entrySet.getValue())) // consolidate the populated hashmap into stream of WordCount objects and return for a new flux
                ));
    }

    protected Flux<String> scrapeWords(String stringOfWords) {
        return Flux.fromArray(
                format(stringOfWords)
                        .split("[^\\w']+") // filter alphanumeric words
        );
    }

    private String format(String stringOfWords) {
        return stringOfWords.toLowerCase(); // assume word count shouldn't be case-sensitive
    }

}
