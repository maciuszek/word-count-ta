package com.maciuszek.wordcount.service;

public interface CountService<T, E>  {

    T count(E s);

    default String format(String stringOfWords) {
        return stringOfWords.toLowerCase(); // assume word count shouldn't be case-sensitive
    }

}
