package com.maciuszek.wordcount.service;

import com.maciuszek.wordcount.domain.WordCount;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Stream;

/**
 * Active sort counter
 *
 * @implNote consistent output for data with the same word frequency but in different order is not ensured
 * @deprecated the sorting doesn't meet all business cases {@link BasicCountService} should be used instead
 */
@Deprecated(since = "0.0.1-SNAPSHOT")
@Service
public class ActiveSortingCountService implements CountService<Flux<WordCount>, Flux<String>> {

    @NoArgsConstructor
    private static class WordCountNode extends WordCount {

        WordCountNode next;
        WordCountNode prev;

        boolean frequencyHead = false;

        WordCountNode(String word) {
            super(word, 0);
        }

        WordCountNode(boolean frequencyHead, int count) {
            super("", count);
            this.frequencyHead = frequencyHead;
        }

    }

    public Flux<WordCount> count(Flux<String> stream) {
        // count words using hash key collision in hashmap and actively sort the order of a linked list of references to the map using some kind of O(1) lfu algorithm derived/inspired by http://dhruvbird.com/lfu.pdf
        Map<String, WordCountNode> wordMap = new HashMap<>();
        Map<Integer, WordCountNode> wordFrequencyMap = new HashMap<>();

        WordCountNode head = new WordCountNode();
        WordCountNode tail = new WordCountNode();
        head.next = tail;
        tail.prev = head;

        return stream.flatMap(this::scrapeWords)
                .doOnNext(word -> {
                    WordCountNode wordNode = wordMap.get(word);
                    if (wordNode == null) {
                        wordNode = new WordCountNode(word);

                        // it's probably faster having next and prev set, than including conditions for null checking per iteration, so redundantly insert wordNode to end of list
                        wordNode.prev = tail.prev;
                        wordNode.next = tail;
                        tail.prev.next = wordNode;
                        tail.prev = wordNode;

                        wordMap.put(word, wordNode);
                    }

                    int newFrequency = wordNode.getCount() + 1;
                    wordNode.setCount(newFrequency);

                    WordCountNode frequencyHead = wordFrequencyMap.get(newFrequency);
                    if (frequencyHead == null) {
                        frequencyHead = new WordCountNode(true, newFrequency);

                        frequencyHead.next = head.next;
                        frequencyHead.prev = head;

                        head.next.prev = frequencyHead;
                        head.next = frequencyHead;

                        wordFrequencyMap.put(wordNode.getCount(), frequencyHead);
                    }

                    // remove wordNode from list
                    wordNode.prev.next = wordNode.next;
                    wordNode.next.prev = wordNode.prev;

                    // insert workNode to new position in list
                    wordNode.next = frequencyHead.next;
                    wordNode.prev = frequencyHead;
                    frequencyHead.next.prev = wordNode;
                    frequencyHead.next = wordNode;
                }) // since this implies potentially infinite vertical growth of memory, instead of a hashmap for wordMap we would probably want to use a key-value database (one that supports reactive connectivity and can be scaled horizontally) then consolidate as a reactive stream from the database
                .thenMany(Flux.fromStream(() -> {
                    // consolidate the created hashmap into a stream derived from the hopefully sorted linked list and capture it as a new flux
                    Stream.Builder<WordCount> streamBuilder = Stream.builder();

                    // at this point the linked list are already sorted starting from head
                    WordCountNode current = head.next;
                    while (current != tail) {
                        if (!current.frequencyHead) {
                            streamBuilder.add(current);
                        }
                        current = current.next;
                    }

                    return streamBuilder.build();
                }));
    }

    private Flux<String> scrapeWords(String stringOfWords) {
        return Flux.fromArray(
                format(stringOfWords)
                        .split("[^\\w']+") // filter alphanumeric words
        );
    }

    private String format(String stringOfWords) {
        return stringOfWords.toLowerCase(); // assume word count shouldn't be case-sensitive
    }

}
