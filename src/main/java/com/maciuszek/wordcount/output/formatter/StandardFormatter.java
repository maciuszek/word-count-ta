package com.maciuszek.wordcount.output.formatter;

import com.maciuszek.wordcount.domain.WordCount;
import org.springframework.stereotype.Component;

@Component
public class StandardFormatter implements Formatter<String, WordCount> {

    public String format(WordCount wordCount) {
        return String.format("%s: %s", wordCount.getWord(), wordCount.getFrequency());
    }

}
