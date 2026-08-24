package ru.practicum.explore_with_me.analyzer.model.dao;

public record RecommendedEventQueryRecord(
        Long eventId,
        Double score
) {
}