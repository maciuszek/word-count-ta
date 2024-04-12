package com.maciuszek.wordcount;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("volatile on system load")
class WordCountApplicationABTests {

    private final ApplicationContextRunner applicationContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WordCountApplication.class))
            .withPropertyValues("wordcount.dummy=true");

    public record ExecutionTime(long normalExecutionTime, long activeSortingExecutionTime) { }

    public static class LongCaptor {

        private long value;

        public void capture(long value) {
            this.value = value;
        }

    }

    private long runNormal(String filepath) {
        LongCaptor longCaptor = new LongCaptor();
        applicationContextRunner.withPropertyValues("wordcount.sorted=true")
                .run(context -> {
                    assertAll(
                            () -> assertThat(context).hasSingleBean(CommandLineRunner.class),
                            () -> assertThat(context).hasBean("sortedCounter")
                    );

                    CommandLineRunner commandLineRunner = context.getBean(CommandLineRunner.class);

                    long startTime = System.currentTimeMillis();
                    commandLineRunner.run(filepath);
                    longCaptor.capture(System.currentTimeMillis() - startTime);
                });
        return longCaptor.value;
    }

    private long runActiveSorting(String filepath) {
        LongCaptor longCaptor = new LongCaptor();
        applicationContextRunner.withPropertyValues("wordcount.sorted=active")
                .run(context -> {
                    assertAll(
                            () -> assertThat(context).hasSingleBean(CommandLineRunner.class),
                            () -> assertThat(context).hasBean("activeSortCounter")
                    );

                    CommandLineRunner commandLineRunner = context.getBean(CommandLineRunner.class);

                    long startTime = System.currentTimeMillis();
                    commandLineRunner.run(filepath);
                    longCaptor.capture(System.currentTimeMillis() - startTime);
                });
        return longCaptor.value;
    }

    // use multiple runs to reduce system bias
    private void testActiveSortingExecutionTime(int runs, String filepath) {
        List<ExecutionTime> executionTimeList = new LinkedList<>();

        for (int i = 0; i < runs; i++) {
            executionTimeList.add(new ExecutionTime(runNormal(filepath), runActiveSorting(filepath)));
        }

        int fasterActiveSorting = 0;
        int fasterNormalSorting = 0;
        long toalActiveSortingTime = 0;
        long totalNormalTime = 0;
        for (ExecutionTime executionTime : executionTimeList) {
            long activeSortingExecutionTime = executionTime.activeSortingExecutionTime();
            long normalExecutionTime = executionTime.normalExecutionTime();

            if (activeSortingExecutionTime <= normalExecutionTime) {
                ++fasterActiveSorting;
            } else {
                ++fasterNormalSorting;
            }

            toalActiveSortingTime += activeSortingExecutionTime;
            totalNormalTime += normalExecutionTime;

            System.out.printf("activeSortingExecutionTime: %d millis, normalExecutionTime: %d millis%n", activeSortingExecutionTime, normalExecutionTime);
        }

        System.out.printf("activeSortingExecutionTime at least as fast as normalExecutionTime %d times out of %d with an overall time difference of %d millis", fasterActiveSorting, runs, toalActiveSortingTime - totalNormalTime);

        assertTrue(
                fasterActiveSorting >= fasterNormalSorting,
                "activeSortingExecutionTime more often slower normalExecutionTime"
        );
    }

    @Test
    @SneakyThrows
    void realData() {
        int runs = 1000;
        String filepath = "/home/maciuszek/workspace/word-count-ta/src/test/resources/big_input.txt"; // sourced from https://www.gutenberg.org/cache/epub/73371/pg73371.txt with wget
        testActiveSortingExecutionTime(runs, filepath);
    }

    @Test
    @SneakyThrows
    void generatedData() {
        int runs = 100;
        String filepath = "/home/maciuszek/workspace/word-count-ta/src/test/resources/generated_big_input.txt"; // generated with src/test/resources/util/generate_random_text_file_with_words.sh
        testActiveSortingExecutionTime(runs, filepath);
    }

}
