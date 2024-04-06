package com.maciuszek.wordcount.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class WordCount {

    protected String word;
    @Setter
    protected int frequency; // defaults to 0

    protected WordCount(String word) {
        this.word = word;
    }

}
