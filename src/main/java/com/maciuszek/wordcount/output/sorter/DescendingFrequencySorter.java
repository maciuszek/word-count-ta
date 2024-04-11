package com.maciuszek.wordcount.output.sorter;

import com.maciuszek.wordcount.domain.WordCount;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;

@Component
public class DescendingFrequencySorter implements Sorter<Flux<WordCount>> {

    @Override
    public Flux<WordCount> sort(Flux<WordCount> flux) {
        // for production, redundantly a database would need to be utilized again in place of the hashmap as with com.maciuszek.wordcount.service.BasicCountService,
        // since the stream size and implicitly the memory of the hashmap is undefined. a better solution would be active sorting as attempted in
        // https://github.com/maciuszek/word-count-ta/blob/active_sort/src/main/java/com/maciuszek/wordcount/service/ActiveSortingCountService.java

        Map<Integer, List<WordCount>> wordCountFequencyMap = new HashMap<>(); // use a hashmap to group the WordCount objects in the flux by frequency

        return flux.doOnNext(wordCount -> {
            Integer key = wordCount.getCount();
            List<WordCount> wordCountList = wordCountFequencyMap.getOrDefault(key, new ArrayList<>());
            wordCountList.add(wordCount);
            wordCountFequencyMap.put(key, wordCountList);
        }).thenMany(Flux.create(sink -> {
            // once all the data has been consolidated into a hashmap, derive a new flux stream of WordCount objects sorted by frequency
            List<Integer> frequencies = new ArrayList<>(wordCountFequencyMap.keySet());
            frequencies.sort(Collections.reverseOrder()); // sort the list of frequencies in descending order

            for (Integer frequency : frequencies) {
                List<WordCount> wordCountList = wordCountFequencyMap.get(frequency);
                wordCountList.sort(Comparator.comparing(WordCount::getWord)); // per frequency sort the WordCounts alphabetically based on the word to ensure consistency for data with the same word distribution but in different order
                wordCountList.forEach(sink::next);
            }

            sink.complete();
        }));
    }

}
