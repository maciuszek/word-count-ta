package com.maciuszek.wordcount.output;

public interface OutputWriter<T> {

    void write(T s);

}
