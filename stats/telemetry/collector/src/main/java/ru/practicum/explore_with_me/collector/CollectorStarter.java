package ru.practicum.explore_with_me.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class CollectorStarter {
    public static void main(String[] args) {
        SpringApplication.run(CollectorStarter.class, args);
    }
}