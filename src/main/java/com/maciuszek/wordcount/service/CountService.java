package com.maciuszek.wordcount.service;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class CountService {

    @NoArgsConstructor
    private static class CountNode {

        CountNode next;
        CountNode prev;

        String data;
        int frequency;

        CountNode(String data, int frequency) {
            this.data = data;
            this.frequency = frequency;
        }

        void incFrequency() {
            ++frequency;
        }

    }

    public Flux<String> words(Flux<String> stream) {
        Map<String, CountNode> wordCountMap = new HashMap<>();
        Map<Integer, CountNode> frequencyMap = new HashMap<>();
        CountNode head = new CountNode();
        CountNode tail = new CountNode();
        head.next = tail;
        tail.prev = head;
        wordCountMap.put("", head);

        return stream.flatMap(this::scrapeWords)
                .doOnNext(word -> {
                    CountNode countNode = wordCountMap.get(word);
                    if (countNode == null) {
                        countNode = new CountNode(word, 0);

                        countNode.prev = tail.prev;
                        countNode.next = tail;

                        tail.prev.next = countNode;
                        tail.prev = countNode;

                        wordCountMap.put(word, countNode);
                    }
                    countNode.incFrequency();

                    CountNode frequencyNode = frequencyMap.get(countNode.frequency);
                    if (frequencyNode == null) {
                        frequencyMap.put(countNode.frequency, countNode);
                        frequencyNode = head;
                    }

                    countNode.prev.next = countNode.next;
                    countNode.next.prev = countNode.prev;

                    countNode.next = frequencyNode.next;
                    countNode.prev = frequencyNode;

                    frequencyNode.next.prev = countNode;
                    frequencyNode.next = countNode;
                })
                .thenMany(Flux.fromStream(() -> {
                    CountNode current = head.next;
                    List<String> values = new ArrayList<>();
                    while (current != tail) {
                        values.add(String.format("%s: %s", current.data, current.frequency));
                        current = current.next;
                    }
                    return values.stream();
                }));
    }

    private Flux<String> scrapeWords(String s) {
        return Flux.fromArray(s.split("[^\\w']+")); // filter alphanumeric words
    }

}
