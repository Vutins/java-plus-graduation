package ru.practicum.explore_with_me.analyzer.service.recommendation;

import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationsService {
    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);
}