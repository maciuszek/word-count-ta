package com.maciuszek.wordcount.output;

import com.maciuszek.wordcount.domain.WordCount;
import com.maciuszek.wordcount.output.formatter.Formatter;
import org.springframework.stereotype.Component;

/**
 * Write unsorted data to standard output
 */
@Component
public class UnsortedStandardOutputWriter extends StandardOutputWriter {

    public UnsortedStandardOutputWriter(Formatter<String, WordCount> formatter) {
        super(formatter);
    }

}
