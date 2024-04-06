package com.maciuszek.wordcount.output;

public interface OutputWriter<E> {

    void write(E e);

}
