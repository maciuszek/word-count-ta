package com.maciuszek.wordcount;

import com.maciuszek.wordcount.domain.WordCount;
import com.maciuszek.wordcount.output.sorter.Sorter;
import com.maciuszek.wordcount.testutil.ResultCaptor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

@ExtendWith({SpringExtension.class, OutputCaptureExtension.class})
@ContextConfiguration(classes = { WordCountApplication.class }, initializers = ConfigDataApplicationContextInitializer.class)
class WordCountApplicationTests {

	@Autowired
	public CommandLineRunner commandLineRunner;

	@SpyBean
	@Qualifier("descendingFrequencySorter")
	private Sorter<Flux<WordCount>> sorter;

	@Test
	@SneakyThrows
	void expectedOutput(CapturedOutput capturedOutput) {
		commandLineRunner.run("./src/test/resources/input.txt");
		String expectedOutput = Files.readString(Paths.get(getClass().getClassLoader().getResource("output.txt").toURI()));
		assertTrue(
				capturedOutput
						.getOut()
						.contains(expectedOutput)
		);
	}

	public static class DecreasingIntChecker {

		private int current = Integer.MAX_VALUE;

		public boolean isDecreasing(int count) {
			if (count > current) {
				return false;
			}

			current = count;

			return true;
		}

	}
	@Test
	@SneakyThrows
	void correctSorting() {
		ResultCaptor<Flux<WordCount>> resultCaptor = new ResultCaptor<>();
		doAnswer(resultCaptor).when(sorter).sort(any());

		commandLineRunner.run("./src/test/resources/input.txt");

		Flux<WordCount> results = resultCaptor.getResult();

		DecreasingIntChecker decreasingIntChecker = new DecreasingIntChecker();
		StepVerifier.create(results)
				.thenConsumeWhile(wordCount -> decreasingIntChecker.isDecreasing(wordCount.getCount()))
				.verifyComplete();
	}

	@Test
	@SneakyThrows
	void consistentOrder() {
		ResultCaptor<Flux<WordCount>> resultCaptor1 = new ResultCaptor<>();
		doAnswer(resultCaptor1).when(sorter).sort(any());

		commandLineRunner.run("./src/test/resources/input.txt");

		reset(sorter);

		ResultCaptor<Flux<WordCount>> resultCaptor2 = new ResultCaptor<>();
		doAnswer(resultCaptor2).when(sorter).sort(any());

		commandLineRunner.run("./src/test/resources/tupni.txt"); // generated with tac

		assertEquals(resultCaptor1.getResult().collectList().block().toString(), resultCaptor2.getResult().collectList().block().toString());
	}

}
