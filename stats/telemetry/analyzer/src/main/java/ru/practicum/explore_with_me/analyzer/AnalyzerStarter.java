package ru.practicum.explore_with_me.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerStarter {
    public static void main(String[] args) {
        log.info("ANALYZER STARTED");
        SpringApplication.run(AnalyzerStarter.class, args);
    }
}