package ru.practicum.explore_with_me.analyzer.service.similarity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.analyzer.model.dao.Similarity;
import ru.practicum.explore_with_me.analyzer.model.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final SimilarityRepository similarityRepository;

    @Override
    public void updateEventSimilarity(EventSimilarityAvro event) {
        Optional<Similarity> similarityOpt = similarityRepository.findByEventId1AndEventId2(event.getEventA(), event.getEventB());
        Similarity similarity;
        if (similarityOpt.isPresent()) {
            similarity = similarityOpt.get();
            similarity.setSimilarity(event.getScore());
        } else {
            similarity = Similarity.builder()
                    .eventId1(event.getEventA())
                    .eventId2(event.getEventB())
                    .similarity(event.getScore())
                    .ts(LocalDateTime.ofInstant(event.getTimestamp(), ZoneId.systemDefault()))
                    .build();
        }
        similarityRepository.save(similarity);
    }
}