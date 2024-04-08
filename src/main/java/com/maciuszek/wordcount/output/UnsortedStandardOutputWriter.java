package com.maciuszek.wordcount.output;

import com.maciuszek.wordcount.domain.WordCount;
import com.maciuszek.wordcount.output.formatter.Formatter;
import com.maciuszek.wordcount.output.sorter.NaturalOrderSorter;
import com.maciuszek.wordcount.output.sorter.Sorter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Write unsorted data to standard output
 */
@Component
public class UnsortedStandardOutputWriter extends StandardOutputWriter {

    private final Sorter<Flux<WordCount>> sorter;

    public UnsortedStandardOutputWriter(NaturalOrderSorter naturalOrderSorter, Formatter<String, WordCount> formatter) {
        super(formatter);
        this.sorter = naturalOrderSorter;
    }

    @Override
    public void write(Flux<WordCount> flux) {
        super.write(sorter.sort(flux)); // sort the data before output
    }

}
