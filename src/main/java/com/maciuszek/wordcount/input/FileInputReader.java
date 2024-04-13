package com.maciuszek.wordcount.input;

import com.maciuszek.wordcount.exception.FileException;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FileInputReader implements InputReader<Flux<String>, File> {

    private static final int MAXIMUM_WORD_SIZE = 1000;

    // todo see if I can speed up the bottleneck introduced by reading 1 character from the file at a time, building a word manually and ensuring a maximum buffer size
    @Override
    public Flux<String> read(File file) {
        AtomicInteger bufferSize = new AtomicInteger(0);
        return DataBufferUtils.readAsynchronousFileChannel(
                () -> AsynchronousFileChannel.open(
                        Path.of(file.getAbsolutePath()),
                        StandardOpenOption.READ
                ),
                new DefaultDataBufferFactory(),
                1) // read and stream one character at a time avoid over allocating memory (e.g. very long line)
                .map(dataBuffer -> (char) dataBuffer.read())
                .bufferWhile(character -> {
                    boolean isWordFinished = character != ' ' && character != ',' && character != '.' && character != '\n';
                    if (isWordFinished) {
                        bufferSize.set(0);
                    } else {
                        // use maximum file size to ensure we will not exceed memory
                        if (bufferSize.incrementAndGet() > MAXIMUM_WORD_SIZE) {
                            throw new FileException(FileException.Type.WORD_TOO_BIG, file.getPath());
                        }
                    }
                    return isWordFinished;
                }) // build words todo this word building criteria probably needs adjustment
                .map(characterList -> {
                    StringBuilder sb = new StringBuilder();
                    for (Character c : characterList) {
                        sb.append(c);
                    }
                    return sb.toString();
                });
    }

}
