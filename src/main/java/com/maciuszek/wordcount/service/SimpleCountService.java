package com.maciuszek.wordcount.service;

import com.maciuszek.wordcount.domain.WordCount;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * Simple counter
 *
 * @implNote the count results are not sorted
 * @deprecated for {@link ActiveSortingCountService} which should be more efficient
 */
@Deprecated(since = "0.0.2-SNAPSHOT")
@Service
public class SimpleCountService implements CountService<Flux<WordCount>, Flux<String>> {

    @Override
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

}
