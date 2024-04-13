package com.maciuszek.wordcount.output;

import com.maciuszek.wordcount.configuration.WordCountConfiguration;
import com.maciuszek.wordcount.domain.WordCount;
import com.maciuszek.wordcount.output.formatter.Formatter;
import com.maciuszek.wordcount.output.sorter.DescendingFrequencySorter;
import com.maciuszek.wordcount.output.sorter.Sorter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Write sorted data to standard output
 */
@Component
public class SortedStandardOutputWriter extends StandardOutputWriter {

    private final Sorter<Flux<WordCount>> sorter;

    public SortedStandardOutputWriter(DescendingFrequencySorter descendingFrequencySorter,
                                      WordCountConfiguration wordCountConfiguration,
                                      Formatter<String, WordCount> formatter) {
        super(wordCountConfiguration, formatter);
        this.sorter = descendingFrequencySorter;
    }

    @Override
    public void write(Flux<WordCount> flux) {
        super.write(sorter.sort(flux)); // sort the data before output
    }

}
