package com.maciuszek.wordcount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WordCountApplication {

	// todo add more debug/performance logging things like words processed
	// todo add debug logging and/or metrics for things like memory usage/hashmap size
	// todo add javadocs

	public static void main(String[] args) {
		// currently the only entrypoint is com.maciuszek.wordcount.runner.WordCountCommandLineRunner
		SpringApplication.run(WordCountApplication.class, args);
	}

}
