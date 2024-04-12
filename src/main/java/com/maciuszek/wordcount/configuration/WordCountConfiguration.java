package com.maciuszek.wordcount.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("wordcount")
@Getter
@Setter
public class WordCountConfiguration {

    private boolean dummy; // no output

}
