package com.maciuszek.wordcount.service;

import com.maciuszek.wordcount.domain.WordCount;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class BasicCountService implements CountService<Flux<WordCount>, Flux<String>> {

    public Flux<WordCount> count(Flux<String> flux) {
        // for production, a database would need to be utilized in place of the hashmap since the stream size and
        // implicitly the memory of the hashmap is undefined. we would probably want to use a key-value database (one
        // that supports reactive connectivity and can be scaled horizontally) and return a reactive stream from
        // the database once all the data in the stream has been written

        Map<String, Integer> wordCountMap = new HashMap<>(); // count words using hash key collision in hashmap

        return flux.flatMap(this::scrapeWords)
                .doOnNext(word -> wordCountMap.compute(
                        word,
                        (key, val) -> (val == null) ? 1 : val + 1
                ))
                .thenMany(Flux.fromStream(() ->
                    wordCountMap.entrySet().stream()
                            .map(entrySet -> new WordCount(entrySet.getKey(), entrySet.getValue())) // once the data has been consolidated into a hashmap return a stream entries mapped to WordCount objects as a new flux
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
