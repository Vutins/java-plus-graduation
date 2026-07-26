package ru.practicum.explore_with_me.analyzer.processor.similarities.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Properties;

@Getter
@Setter
@ConfigurationProperties("analyzer.kafka-consumer-similarity")
@RequiredArgsConstructor
public class EventSimilaritiesKafkaConfig {
    private final Properties properties;
    private final String topic;
    private final Duration timeout;
}