package ru.practicum.explore_with_me.analyzer.model.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore_with_me.analyzer.model.dao.RecommendedEventQueryRecord;
import ru.practicum.explore_with_me.analyzer.model.dao.Similarity;

import java.util.List;
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    Optional<Similarity> findByEventId1AndEventId2(Long eventId1, Long eventId2);

    @Query("""
            SELECT new ru.practicum.explore_with_me.analyzer.model.dao.RecommendedEventQueryRecord(
                     CASE WHEN s.eventId1 = :eventId THEN s.eventId2 ELSE s.eventId1 END,
                     s.similarity)
              FROM Similarity s
             WHERE :eventId IN (s.eventId1, s.eventId2)
               AND NOT EXISTS (SELECT 'no user interactions'
                                 FROM Interaction i
                                WHERE i.userId = :userId
                                  AND i.eventId = CASE
                                                    WHEN s.eventId1 = :eventId THEN s.eventId2
                                                    ELSE s.eventId1
                                                  END)
             ORDER BY s.similarity DESC
            """)
    List<RecommendedEventQueryRecord> getSimilarEvents(@Param("userId") Long userId, @Param("eventId") Long eventId, Limit limit);

    @Query("""
            SELECT similar_eventId
              FROM (SELECT CASE
                             WHEN s.eventId1 = i.eventId THEN s.eventId2
                             ELSE s.eventId1
                           END similar_eventId,
                           MAX(s.similarity) similarity_max
                      FROM Interaction i
                           JOIN Similarity s
                             ON i.eventId IN (s.eventId1, s.eventId2)
                     WHERE i.userId = :userId
                       AND NOT EXISTS (SELECT 'no user interactions'
                                         FROM Interaction i2
                                        WHERE i2.userId = :userId
                                          AND i2.eventId = CASE
                                                             WHEN s.eventId1 = i.eventId THEN s.eventId2
                                                             ELSE s.eventId1
                                                           END)
                     GROUP BY CASE
                                WHEN s.eventId1 = i.eventId THEN s.eventId2
                                ELSE s.eventId1
                              END)
             ORDER BY similarity_max DESC
            """)
    List<Long> getSimilarEventIdsForUser(@Param("userId") Long userId, Limit limit);

    @Query("""
            SELECT sum(i.rating * s.similarity) / sum(s.similarity)
              FROM Similarity s
                   JOIN Interaction i
                     ON i.eventId IN (s.eventId1, s.eventId2)
                    AND i.eventId != :eventId
                    AND i.userId = :userId
             WHERE :eventId IN (s.eventId1, s.eventId2)
            """)
    Optional<Double> calculateRating(@Param("userId") Long userId, @Param("eventId") Long eventId);
}