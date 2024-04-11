package com.maciuszek.wordcount.service;

import com.maciuszek.wordcount.domain.WordCount;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class ActiveSortingCountService extends BasicCountService {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class WordCountNode extends WordCount {

        private WordCountNode next;
        private WordCountNode prev;

        private boolean frequencyHead = false;

        private WordCountNode(String word) {
            super(word, 0);
        }

        private WordCountNode(boolean frequencyHead, int count) {
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
        Map<Integer, WordCountNode> wordFrequencyMap = new LinkedHashMap<>();

        WordCountNode head = new WordCountNode();
        WordCountNode tail = new WordCountNode(true, 0); // make tail a frequency had to optimize stream building
        head.next = tail;
        tail.prev = head;
        wordFrequencyMap.put(0, tail);

        return stream.flatMap(this::scrapeWords)
                .doOnNext(word -> {
                    WordCountNode wordNode = wordMap.get(word);
                    if (wordNode == null) {
                        // if a word isn't already in the map, add it
                        wordNode = new WordCountNode(word);
                        wordMap.put(word, wordNode);
                    } else {
                        // remove wordNode from list since it will be re-added in new position
                        wordNode.prev.next = wordNode.next;
                        wordNode.next.prev = wordNode.prev;
                    }

                    int newFrequency = wordNode.getCount() + 1;
                    wordNode.setCount(newFrequency);

                    // todo get and nullcheck may be faster
                    WordCountNode frequencyHead = wordFrequencyMap.computeIfAbsent(newFrequency, key -> {
                        // if the frequency hasn't been previously added create a new head for the current frequency and add it to the top of the linked list
                        // always adding to the top this lists works since the frequencies will grow in linear order
                        // note: there will be a frequency head for 1 - highest frequency, even if there are no words anymore for the specific frequency

                        WordCountNode newFrequencyHead = new WordCountNode(true, key);

                        newFrequencyHead.next = head.next;
                        newFrequencyHead.prev = head;

                        head.next.prev = newFrequencyHead;
                        head.next = newFrequencyHead;

                        return newFrequencyHead;
                    });

                    // insert workNode a new position in the list based on the new frequency
                    wordNode.next = frequencyHead.next;
                    wordNode.prev = frequencyHead;
                    frequencyHead.next.prev = wordNode;
                    frequencyHead.next = wordNode;
                })
                .thenMany(Flux.create(sink -> {
                    // once all the data in the stream has been consolidated, use the linked list to build a stream of WordCount elements in sorted order and return it as a new flux
                    List<WordCountNode> frequencyHeadList = new ArrayList<>(wordFrequencyMap.values()); // assumes natural order of wordFrequencyMap which is deterministic by LinkedHashMap

                    for (int i = frequencyHeadList.size() - 1; i > 0; i--) {
                        // sort alphabetically per frequency to ensure consistency for data with the same word distribution but in different order. do this after the data has been counted otherwise we would introduce a '* n' in time complexity
                        TreeSet<WordCount> alphabeticallySortedSet = new TreeSet<>(Comparator.comparing(WordCount::getWord));

                        WordCountNode current = frequencyHeadList.get(i);
                        while (!(current = current.next).frequencyHead) {
                            alphabeticallySortedSet.add(current);
                        }

                        alphabeticallySortedSet.forEach(sink::next);
                    }

                    sink.complete();
                }));
    }

}
