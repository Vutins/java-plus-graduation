package ru.practicum.explore_with_me.analyzer.processor.actions.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Properties;

@Getter
@Setter
@ConfigurationProperties("analyzer.kafka-consumer-actions")
@RequiredArgsConstructor
public class UserActionsKafkaConfig {
    private final Properties properties;
    private final String topic;
    private final Duration timeout;
}