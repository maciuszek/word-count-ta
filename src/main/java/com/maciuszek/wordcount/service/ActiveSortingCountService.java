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
public class ActiveSortingCountService extends BasicCountService {

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

    /**
     * Count words in a stream using hash key collision in hashmap and actively keep track of the frequency order in a linked list.
     * note: implemented with an O(1) lfu algorithm to keep the linked list sorted inspired by http://dhruvbird.com/lfu.pdf
     *
     * @param stream a flux stream of strings
     * @return a new flux stream of counted words derived from the input stream
     */
    @Override
    public Flux<WordCount> count(Flux<String> stream) {
        // for production, a database would need to be utilized in place of the hashmap since the stream size and
        // implicitly the memory of the hashmap is undefined. we would probably want to use a key-value database (one
        // that supports reactive connectivity and can be scaled horizontally) and return a reactive stream from
        // the database once all the data in the stream has been written

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
                        // if a word isn't already in the map, add it
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
                        // if the frequency hasn't been previously added create a new head for the current frequency and add it to the top of the linked list
                        // always adding to the top this lists works since the frequencies will grow in linear order
                        // note: there will be a frequency head for 1 - highest frequency, even if there are no words anymore for the specific frequency

                        frequencyHead = new WordCountNode(true, newFrequency);

                        frequencyHead.next = head.next;
                        frequencyHead.prev = head;

                        head.next.prev = frequencyHead;
                        head.next = frequencyHead;

                        wordFrequencyMap.put(wordNode.getCount(), frequencyHead); // add the frequency head to the list for future reference/index
                    }

                    // remove wordNode from list
                    wordNode.prev.next = wordNode.next;
                    wordNode.next.prev = wordNode.prev;

                    // insert workNode a new position in the list based on the new frequency
                    wordNode.next = frequencyHead.next;
                    wordNode.prev = frequencyHead;
                    frequencyHead.next.prev = wordNode;
                    frequencyHead.next = wordNode;
                })
                .thenMany(Flux.fromStream(() -> {
                    // once all the data in the stream has been consolidated, use the linked list to build a stream of WordCount elements in sorted order and return it as a new flux
                    Stream.Builder<WordCount> streamBuilder = Stream.builder();

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

}
