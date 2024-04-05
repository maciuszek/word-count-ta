package com.maciuszek.wordcount.service;

import com.maciuszek.wordcount.exception.FileException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class FileService {

    // todo maybe need to validate if the path is correct/adheres to host OS
    public File load(String pathToFile) {
        File file = new File(pathToFile);
        validate(file).ifPresent(exceptionType -> {
            throw new FileException(exceptionType, pathToFile);
        });
        return file;
    }

    private Optional<FileException.Type> validate(File file) {
        if (!file.exists()) {
            return Optional.of(FileException.Type.DOES_NOT_EXIST);
        }

        if (!file.canRead()) {
            return Optional.of(FileException.Type.CANNOT_READ);
        }

        return Optional.empty();
    }

}
