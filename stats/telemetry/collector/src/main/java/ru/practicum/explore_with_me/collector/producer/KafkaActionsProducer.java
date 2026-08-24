package ru.practicum.explore_with_me.collector.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.explore_with_me.collector.config.KafkaConfig;

@Component
public class KafkaActionsProducer implements AutoCloseable {
    private final KafkaProducer<String, UserActionAvro> kafkaProducer;
    private final String userActionTopic;

    public KafkaActionsProducer(KafkaConfig kafkaConfig) {
        this.kafkaProducer = new KafkaProducer<>(kafkaConfig.getProperties());
        this.userActionTopic = kafkaConfig.getTopic();
    }

    public void send(UserActionAvro userAction) {
        kafkaProducer.send(new ProducerRecord<>(userActionTopic, userAction));
    }

    public void flush() {
        kafkaProducer.flush();
    }

    @Override
    public void close() throws Exception {
        kafkaProducer.close();
    }
}