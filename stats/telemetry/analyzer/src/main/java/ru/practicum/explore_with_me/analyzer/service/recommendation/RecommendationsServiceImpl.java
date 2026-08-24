package ru.practicum.explore_with_me.analyzer.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.analyzer.model.repository.InteractionRepository;
import ru.practicum.explore_with_me.analyzer.model.repository.SimilarityRepository;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationsServiceImpl implements RecommendationsService {

    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    @Override
    public List<RecommendedEventProto> getInteractionsCount(final InteractionsCountRequestProto request) {
        return interactionRepository.getInteractionsSumByEventIds(request.getEventIdList()).stream()
                .map(rec -> RecommendedEventProto.newBuilder()
                        .setEventId(rec.eventId())
                        .setScore(rec.score())
                        .build())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(final SimilarEventsRequestProto request) {
        return similarityRepository.getSimilarEvents(request.getUserId(), request.getEventId(), Limit.of((int) request.getMaxResults())).stream()
                .map(rec -> RecommendedEventProto.newBuilder()
                        .setEventId(rec.eventId())
                        .setScore(rec.score())
                        .build())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(final UserPredictionsRequestProto request) {
        return similarityRepository.getSimilarEventIdsForUser(request.getUserId(), Limit.of((int) request.getMaxResults())).stream()
                .map(eventId -> RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(similarityRepository.calculateRating(request.getUserId(), eventId).orElse(0D))
                        .build())
                .toList();
    }
}