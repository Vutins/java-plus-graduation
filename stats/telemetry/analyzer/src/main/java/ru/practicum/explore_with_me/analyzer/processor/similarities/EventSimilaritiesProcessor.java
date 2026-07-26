package ru.practicum.explore_with_me.analyzer.processor.similarities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.analyzer.processor.similarities.config.EventSimilaritiesKafkaConfig;
import ru.practicum.explore_with_me.analyzer.service.similarity.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilaritiesProcessor implements Runnable {

    private final EventSimilaritiesKafkaConfig kafkaConfig;
    private final EventSimilarityService similarityService;

    @Override
    public void run() {
        KafkaConsumer<String, EventSimilarityAvro> consumer = new KafkaConsumer<>(kafkaConfig.getProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(kafkaConfig.getTopic()));

            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(kafkaConfig.getTimeout());

                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    similarityService.updateEventSimilarity(record.value());
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

}