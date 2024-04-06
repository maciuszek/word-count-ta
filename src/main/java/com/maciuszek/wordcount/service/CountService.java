package com.maciuszek.wordcount.service;

public interface CountService<T, E>  {

    T count(E s);

}
