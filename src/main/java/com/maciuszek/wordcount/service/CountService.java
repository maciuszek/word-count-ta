package com.maciuszek.wordcount.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
public class CountService {

    public Flux<String> words(Flux<String> stream) {
        Map<String, Integer> wordCountMap = new HashMap<>();
        return stream.flatMap(this::scrapeWords)
                .doOnNext(word -> wordCountMap.compute(
                        word,
                        (key, val) -> (val == null) ? 1 : val + 1
                )) // todo improve readability
                .thenMany(Flux.fromIterable(wordCountMap.keySet())
                        .sort(Comparator.comparingInt(wordCountMap::get).reversed()) // todo optimize this and sort/reorder actively
                        .map(key -> String.format("%s: %s", key, wordCountMap.get(key)))
                );
    }

    private Flux<String> scrapeWords(String s) {
        return Flux.fromArray(s.split("[^\\w']+")); // filter alphanumeric words
    }

}
