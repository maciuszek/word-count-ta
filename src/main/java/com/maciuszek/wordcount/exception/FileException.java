package com.maciuszek.wordcount.exception;

import lombok.Getter;

public class FileException extends RuntimeException {

    public enum Type {
        DOES_NOT_EXIST("File does not exist"), // todo parameterize
        CANNOT_READ("Cannot read the file"); // todo parameterize

        Type(String description) {
            this.description = description;
        }

        @Getter
        private final String description;
    }

    public FileException(Type type, String message) {
        super(String.format("%s: %s", type.getDescription(), message));
    }

}
