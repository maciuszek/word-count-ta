package com.maciuszek.wordcount.output;

import com.maciuszek.wordcount.configuration.WordCountConfiguration;
import com.maciuszek.wordcount.domain.WordCount;
import com.maciuszek.wordcount.output.formatter.Formatter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public abstract class StandardOutputWriter implements OutputWriter<Flux<WordCount>> {

    private final WordCountConfiguration wordCountConfiguration;
    private final Formatter<String, WordCount> formatter;

    @Override
    public void write(Flux<WordCount> flux) {
        if (wordCountConfiguration.isDummy()) {
            flux.subscribe();
        } else {
            flux.map(formatter::format) // format the data for printing
                    .subscribe(System.out::println);
        }
    }

}
