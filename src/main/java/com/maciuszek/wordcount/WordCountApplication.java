package com.maciuszek.wordcount;

import com.maciuszek.wordcount.domain.WordCount;
import com.maciuszek.wordcount.input.FileInputReader;
import com.maciuszek.wordcount.input.InputReader;
import com.maciuszek.wordcount.output.OutputWriter;
import com.maciuszek.wordcount.output.SortedStandardOutputWriter;
import com.maciuszek.wordcount.output.UnsortedStandardOutputWriter;
import com.maciuszek.wordcount.service.ActiveSortingCountService;
import com.maciuszek.wordcount.service.CountService;
import com.maciuszek.wordcount.service.SimpleCountService;
import com.maciuszek.wordcount.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.Arrays;

@SpringBootApplication
@Slf4j
public class WordCountApplication {

	// todo add unit tests
	// todo add more debug/performance logging things like words processed
	// todo add debug logging and/or metrics for things like memory usage/hashmap size
	// todo add more javadocs

	public static void main(String[] args) {
		SpringApplication.run(WordCountApplication.class, args);
	}


	// a less naive counter workflow
	@ConditionalOnProperty(name = "wordcount.sorted", havingValue = "active")
	@Bean
	CommandLineRunner activelySortedCounter(FileService fileService,
									FileInputReader fileInputReader,
									ActiveSortingCountService activeSortingCountService,
									UnsortedStandardOutputWriter unsortedStandardOutputWriter) {
		return args -> run(args, fileService, fileInputReader, activeSortingCountService, unsortedStandardOutputWriter);
	}

	@ConditionalOnProperty(name = "wordcount.sorted", havingValue = "true")
	@Bean
	CommandLineRunner sortedCounter(FileService fileService,
									FileInputReader fileInputReader,
									SimpleCountService basicCountService,
									SortedStandardOutputWriter sortedStandardOutputWriter) {
		return args -> run(args, fileService, fileInputReader, basicCountService, sortedStandardOutputWriter);
	}

	@ConditionalOnMissingBean(CommandLineRunner.class)
	@Bean
	CommandLineRunner counter(FileService fileService,
							  FileInputReader fileInputReader,
							  SimpleCountService basicCountService,
							  UnsortedStandardOutputWriter unsortedStandardOutputWriter) {
		return args -> run(args, fileService, fileInputReader, basicCountService, unsortedStandardOutputWriter);
	}

	// provision the count workflow engine in
	// 3 phases: read, count, write
	// 2 sub-phases: sort, format
	private void run(String[] args,
					 FileService fileService,
					 InputReader<Flux<String>, File> inputReader,
					 CountService<Flux<WordCount>, Flux<String>> countService,
					 OutputWriter<Flux<WordCount>> outputWriter) {
		log.debug("Starting word count for {}", Arrays.toString(args));
		long startTime = System.currentTimeMillis();

		// support for reading multiple files into a single stream
		Flux<String> input = Flux.empty();
		for (String arg : args) {
			File file = fileService.load(arg);
			input = Flux.merge(input, inputReader.read(file));
		}

		outputWriter.write(countService.count(input));

		log.debug("Finished counting words in {} millis", System.currentTimeMillis() - startTime);
	}

}
