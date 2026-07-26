package ru.practicum.explore_with_me.aggregator.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.explore_with_me.aggregator.consumer.config.KafkaConsumerConfig;
import ru.practicum.explore_with_me.aggregator.producer.KafkaSimilarityProducer;
import ru.practicum.explore_with_me.aggregator.storage.EventSimilarityStorage;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final EventSimilarityStorage eventSimilarityStorage;
    private final KafkaConsumerConfig kafkaConsumerConfig;
    private final KafkaSimilarityProducer producer;

    public void start() {
        KafkaConsumer<String, UserActionAvro> consumer = new KafkaConsumer<>(kafkaConsumerConfig.getProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(kafkaConsumerConfig.getTopic()));

            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(kafkaConsumerConfig.getTimeout());

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    handleRecord(record);
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void handleRecord(ConsumerRecord<String, UserActionAvro> record) throws InterruptedException {
        eventSimilarityStorage.getUpdatedSimilarities(record.value()).forEach(
                event -> producer.send(event, Instant.now())
        );
        producer.flush();
    }

}