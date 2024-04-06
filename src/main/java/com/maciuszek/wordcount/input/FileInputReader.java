package com.maciuszek.wordcount.input;

import com.maciuszek.wordcount.exception.FileException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Component
public class FileInputReader implements InputReader<Flux<String>, File> {

    @Override
    public Flux<String> read(File file) {
        return Flux.using(
                () -> new FileReader(file),
                reader -> Flux.fromStream(new BufferedReader(reader).lines()),
                reader -> {
                    try {
                        reader.close(); // todo refactor non-blocking
                    } catch (IOException e) {
                        throw new FileException(FileException.Type.ERROR_CLOSING_FILE_READER, e);
                    }
                }
        );
    }

}
