package com.maciuszek.wordcount.runner;

import com.maciuszek.wordcount.domain.WordCount;
import com.maciuszek.wordcount.input.InputReader;
import com.maciuszek.wordcount.output.OutputWriter;
import com.maciuszek.wordcount.service.CountService;
import com.maciuszek.wordcount.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class WordCountCommandLineRunner implements CommandLineRunner {

    private final InputReader<Flux<String>, File>  inputReader;
    private final OutputWriter<Flux<WordCount>> outputWriter;
    private final FileService fileService;
    private final CountService<Flux<WordCount>, Flux<String>> countService;

    @Override
    public void run(String... args) {
        log.debug("Starting word count for {}", Arrays.toString(args));
        long startTime = System.currentTimeMillis();

        Flux<String> input = Flux.empty();
        for (String arg : args) {
            File file = fileService.load(arg);
            input = Flux.merge(input, inputReader.read(file));
        }
        outputWriter.write(countService.count(input));


        log.debug("Finished counting words in {} millis", System.currentTimeMillis() - startTime);
    }

}
