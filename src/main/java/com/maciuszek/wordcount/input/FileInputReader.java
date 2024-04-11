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
                        reader.close(); // this should be fine since the file stream itself is non-blocking and once the complete file is processed the application runtime is complete
                    } catch (IOException e) {
                        throw new FileException(FileException.Type.ERROR_CLOSING_FILE_READER, e);
                    }
                }
        );
    }

}
