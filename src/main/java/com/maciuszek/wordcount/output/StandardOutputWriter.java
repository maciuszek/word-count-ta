package com.maciuszek.wordcount.output;

import com.maciuszek.wordcount.domain.WordCount;
import com.maciuszek.wordcount.output.formatter.Formatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class StandardOutputWriter implements OutputWriter<Flux<WordCount>> {

    private final Formatter<String, WordCount> formatter;

    @Override
    public void write(Flux<WordCount> s) {
        s.map(formatter::format)
                .subscribe(System.out::println);
    }

}
