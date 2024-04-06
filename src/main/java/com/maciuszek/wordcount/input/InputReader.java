package com.maciuszek.wordcount.input;

public interface InputReader<T, E> {

    T read(E e);

}
