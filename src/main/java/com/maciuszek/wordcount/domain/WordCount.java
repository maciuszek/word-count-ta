package com.maciuszek.wordcount.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class WordCount {

    protected String word;
    @Setter
    protected int count;

    public WordCount(String word, int count) {
        this.word = word;
        this.count = count;
    }

    // unnecessary but used to facilitate com.maciuszek.wordcount.WordCountApplicationTests#consistentOrder()
    public String toString() {
        return word + count;
    }

}
