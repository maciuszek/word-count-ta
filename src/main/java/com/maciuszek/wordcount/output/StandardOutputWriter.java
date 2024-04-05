package com.maciuszek.wordcount.output;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class StandardOutputWriter implements OutputWriter<Flux<String>> {

    @Override
    public void write(Flux<String> s) {
        s.subscribe(System.out::println);
    }

}
