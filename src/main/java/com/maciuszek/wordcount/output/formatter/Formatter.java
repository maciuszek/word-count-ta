package com.maciuszek.wordcount.output.formatter;

public interface Formatter<T, E> {

    T format(E e);

}
