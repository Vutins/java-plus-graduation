package ru.practicum.explore_with_me.aggregator.consumer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Properties;

@Getter
@Setter
@ConfigurationProperties("aggregator.kafka-consumer")
public class KafkaConsumerConfig {
    private final Properties properties;
    private final String topic;
    private final Duration timeout;

    public KafkaConsumerConfig(Properties properties, String topic, Integer timeout) {
        this.properties = properties;
        this.topic = topic;
        this.timeout = Duration.ofMillis(timeout);
    }
}