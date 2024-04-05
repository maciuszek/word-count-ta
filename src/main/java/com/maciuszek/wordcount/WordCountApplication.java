package com.maciuszek.wordcount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WordCountApplication {

	// todo add debug logging
	// todo add tests

	public static void main(String[] args) {
		SpringApplication.run(WordCountApplication.class, args);
	}

}
