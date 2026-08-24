package ru.practicum.explore_with_me.analyzer.service.similarity;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventSimilarityService {
    void updateEventSimilarity(EventSimilarityAvro event);
}