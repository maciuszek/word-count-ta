package com.maciuszek.wordcount.exception;

import lombok.Getter;

// todo suppress errors unless in debug mode
public class FileException extends RuntimeException {

    @Getter
    public enum Type {
        // todo think about using external message sources
        DOES_NOT_EXIST("File does not exist"),
        CANNOT_READ("Cannot read the file"),
        WORD_TOO_BIG("File contains words exceeding maximum size");

        Type(String description) {
            this.description = description;
        }

        private final String description;
    }

    public FileException(Type type, String filepath) {
        super(String.format("%s: %s", type.getDescription(), filepath));
    }


}
