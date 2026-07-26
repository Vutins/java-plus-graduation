package ru.practicum.explore_with_me.aggregator.storage;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface EventSimilarityStorage {
    List<EventSimilarityAvro> getUpdatedSimilarities(UserActionAvro action);
}