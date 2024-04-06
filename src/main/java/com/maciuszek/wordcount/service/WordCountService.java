package com.maciuszek.wordcount.service;

import com.maciuszek.wordcount.domain.WordCount;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class WordCountService implements CountService<Flux<WordCount>, Flux<String>> {

    @NoArgsConstructor
    private static class WordCountNode extends WordCount {

        WordCountNode next;
        WordCountNode prev;

        WordCountNode(String word) {
            super(word);
        }

    }

    public Flux<WordCount> count(Flux<String> stream) {
        // count words using hash key collision in hashmap and actively sort the order of a linked list of references to the map using some kind of O(1) lfu algorithm derived/inspired by starring at http://dhruvbird.com/lfu.pdf for a few minutes
        Map<String, WordCountNode> wordMap = new HashMap<>();
        Map<Integer, WordCountNode> wordFrequencyMap = new HashMap<>();

        WordCountNode head = new WordCountNode();
        WordCountNode tail = new WordCountNode();
        head.next = tail;
        tail.prev = head;
        wordMap.put("", head);

        return stream.flatMap(this::scrapeWords)
                .doOnNext(word -> {
                    WordCountNode wordNode = wordMap.get(word);
                    if (wordNode == null) {
                        wordNode = new WordCountNode(word);

                        // it's probably faster setting nulls than having a condition so redundantly insert wordNode to end of list
                        wordNode.prev = tail.prev;
                        wordNode.next = tail;
                        tail.prev.next = wordNode;
                        tail.prev = wordNode;

                        wordMap.put(word, wordNode);
                    }

                    int newFrequency = wordNode.getFrequency() + 1;
                    wordNode.setFrequency(newFrequency);

                    WordCountNode nodeMatchingFrequency = wordFrequencyMap.get(newFrequency);
                    if (nodeMatchingFrequency == null) {
                        wordFrequencyMap.put(wordNode.getFrequency(), wordNode); // add to frequency map if non exist to be used for future position mapping
                        nodeMatchingFrequency = head; // use head if none exist to be inserted at the top of the list
                    }

                    // remove wordNode from list
                    wordNode.prev.next = wordNode.next;
                    wordNode.next.prev = wordNode.prev;

                    // insert workNode to new position in list
                    wordNode.next = nodeMatchingFrequency.next;
                    wordNode.prev = nodeMatchingFrequency;
                    nodeMatchingFrequency.next.prev = wordNode;
                    nodeMatchingFrequency.next = wordNode;
                }) // since this is unmanaged and potentially infinite vertical growth of memory, for production instead of one thenMany, we would probably want to flush the hashmap periodically to a database (one that can be scaled horizontally) then consolidate from the database. allowing a bigger in-memory map will result in a faster processing
                .thenMany(Flux.fromStream(() -> {
                    // consolidate the created hashmap into list of formatted strings using the hopefully sorted linked list and capture a stream of the list as a new flux
                    List<WordCount> values = new ArrayList<>();

                    WordCountNode current = head.next;
                    while (current != tail) {
                        values.add(current);
                        current = current.next;
                    }

                    return values.stream();
                }));
    }

    private Flux<String> scrapeWords(String s) {
        return Flux.fromArray(s.split("[^\\w']+")); // filter alphanumeric words
    }

}