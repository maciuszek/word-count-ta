package com.maciuszek.wordcount.runner;

import com.maciuszek.wordcount.input.FileInputReader;
import com.maciuszek.wordcount.input.InputReader;
import com.maciuszek.wordcount.output.OutputWriter;
import com.maciuszek.wordcount.output.StandardOutputWriter;
import com.maciuszek.wordcount.service.CountService;
import com.maciuszek.wordcount.service.FileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;

@Component
public class WordCountCommandLineRunner implements CommandLineRunner {

    private final InputReader<Flux<String>, File>  inputReader;
    private final OutputWriter<Flux<String>> outputWriter;
    private final FileService fileService;
    private final CountService countService;


    public WordCountCommandLineRunner(FileInputReader fileInputReader,
                                      StandardOutputWriter standardOutputWriter,
                                      FileService fileService,
                                      CountService countService) {
        this.inputReader = fileInputReader;
        this.outputWriter = standardOutputWriter;
        this.fileService = fileService;
        this.countService = countService;
    }

    @Override
    public void run(String... args) {
        Flux<String> input = Flux.empty();
        for (String arg : args) {
            File file = fileService.load(arg);
            input = Flux.merge(input, inputReader.read(file));
        }
        outputWriter.write(countService.words(input));
    }

}
