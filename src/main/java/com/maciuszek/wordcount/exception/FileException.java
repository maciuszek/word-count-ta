package com.maciuszek.wordcount.exception;

import lombok.Getter;

public class FileException extends RuntimeException {

    @Getter
    public enum Type {
        // todo think about using external message sources
        DOES_NOT_EXIST("File does not exist"),
        CANNOT_READ("Cannot read the file"),
        ERROR_CLOSING_FILE_READER("Error closing file");

        Type(String description) {
            this.description = description;
        }

        private final String description;
    }

    public FileException(Type type, String filepath) {
        super(String.format("%s: %s", type.getDescription(), filepath));
    }

    public FileException(Type type, Throwable throwable) {
        super(type.getDescription(), throwable);
    }


}
