package com.maciuszek.wordcount.output.sorter;

import com.maciuszek.wordcount.domain.WordCount;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class NaturalOrderSorter implements Sorter<Flux<WordCount>> {

    @Override
    public Flux<WordCount> sort(Flux<WordCount> flux) {
        return flux;
    }

}
