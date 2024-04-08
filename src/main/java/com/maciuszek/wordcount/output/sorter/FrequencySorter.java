package com.maciuszek.wordcount.output.sorter;

import com.maciuszek.wordcount.domain.WordCount;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Stream;

@Component
public class FrequencySorter implements Sorter<Flux<WordCount>> {

    @Override
    public Flux<WordCount> sort(Flux<WordCount> flux) {
        Map<Integer, List<WordCount>> wordCountFequencyMap = new HashMap<>(); // use a hashmap to group the WordCount objects in the flux by frequency
        return flux.doOnNext(wordCount -> {
            Integer key = wordCount.getCount();
            List<WordCount> wordCountList = wordCountFequencyMap.getOrDefault(key, new ArrayList<>());
            wordCountList.add(wordCount);
            wordCountFequencyMap.put(key, wordCountList);
        }) // for production the sorting algorithm would need to leverage a database that can automatically scale since this assumes an undefined amount of memory. a better solution would be active sorting as attempted with com.maciuszek.wordcount.service.ActiveSortingCountService
        .thenMany(Flux.fromStream(() -> {
            // using the populated hashmap return a new stream of sorted WordCount objects by frequency
            List<Integer> frequencies = new ArrayList<>(wordCountFequencyMap.keySet());
            frequencies.sort(Collections.reverseOrder()); // sort the list of frequencies in descending order

            Stream<WordCount> stream = Stream.empty();
            for (Integer frequency : frequencies) {
                List<WordCount> wordCountList = wordCountFequencyMap.get(frequency);
                wordCountList.sort(Comparator.comparing(WordCount::getWord)); // per frequency sort the WordCounts alphabetically based on the word to ensure consistency for data with the same word distribution but in different order
                stream = Stream.concat(stream, wordCountList.stream());
            }

            return stream;
        }));
    }

}
