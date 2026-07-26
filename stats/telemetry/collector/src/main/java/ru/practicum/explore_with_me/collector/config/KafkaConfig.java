package ru.practicum.explore_with_me.collector.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.avro.serializer.GeneralAvroSerializer;

import java.util.Properties;

@Getter
@Component
public class KafkaConfig {
    private final Properties properties = new Properties(); // Инициализация здесь!

    @Value("${collector.kafka.producer.topic:stats.user-actions.v1}")
    private String topic;

    @Value("${collector.kafka.producer.properties.bootstrap.servers:localhost:9092}")
    private String bootstrapServers;

    @PostConstruct
    public void init() {
        this.properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        this.properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        this.properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                GeneralAvroSerializer.class.getName());
        this.properties.put(ProducerConfig.ACKS_CONFIG, "1");
        this.properties.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        this.properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        this.properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        this.properties.put(ProducerConfig.CLIENT_ID_CONFIG, "telemetry.collector");
        this.properties.put(ProducerConfig.RETRIES_CONFIG, 3);
    }
}