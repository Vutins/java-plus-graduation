package ru.practicum.explore_with_me.aggregator.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EventSimilarityStorageInMemory implements EventSimilarityStorage {
    private final Map<Long, Map<Long, Double>> eventUserWeight = new HashMap<>();  // Веса действий пользователей c мероприятиями (Event-User-Weight)
    private final Map<Long, Double> eventTotalWeight = new HashMap<>();            // Общая сумма весов каждого из мероприятий
    private final Map<Long, Map<Long, Double>> eventPairsWeight = new HashMap<>(); // Сумма минимальных весов для каждой пары мероприятий

    @Override
    public List<EventSimilarityAvro> getUpdatedSimilarities(final UserActionAvro action) {
        log.debug("----------------------------");
        log.debug("Получено сообщение: event={}", action);

        final Long event = action.getEventId();
        final Long user = action.getUserId();

        final Double newActionWeight = switch (action.getActionType()) {
            case VIEW -> 0.4D;
            case REGISTER -> 0.8D;
            case LIKE -> 1D;
        };

        final Double oldActionWeight = getEventUserWeight(event, user);

        if (newActionWeight > oldActionWeight) {
            setEventUserWeight(event, user, newActionWeight);

            log.debug("Добавляем или обновляем дельту изменения к общей сумме мероприятия: oldWeight={}, newWeight={}", oldActionWeight, newActionWeight);
            eventTotalWeight.put(event, eventTotalWeight.getOrDefault(event, 0D) + newActionWeight - oldActionWeight);

            final List<EventSimilarityAvro> eventsSimilarity = new ArrayList<>();

            log.debug("Определяем список мероприятий с которыми взаимодействовал пользователь кроме обновляемого и формируем список событий для оптравки");
            eventUserWeight.entrySet().stream()
                    .filter(e -> (!e.getKey().equals(event)) && (e.getValue().containsKey(user)))
                    .map(Map.Entry::getKey)
                    .forEach(otherEvent -> {
                        updateEventPairWeight(event, otherEvent, user, oldActionWeight);

                        eventsSimilarity.add(EventSimilarityAvro.newBuilder()
                                .setEventA(Math.min(event, otherEvent))
                                .setEventB(Math.max(event, otherEvent))
                                .setScore(calculateEventsScore(event, otherEvent))
                                .setTimestamp(action.getTimestamp())
                                .build());
                    });

            return eventsSimilarity;
        }

        return List.of();
    }

    private Double getEventUserWeight(Long event, Long user) {
        return eventUserWeight.containsKey(event) && eventUserWeight.get(event).containsKey(user) ? eventUserWeight.get(event).get(user) : 0D;
    }

    private void setEventUserWeight(Long event, Long user, Double weight) {
        log.debug("Сохраняем новый вес действия event={}, user={}, weight={}", event, user, weight);
        eventUserWeight.computeIfAbsent(event, e -> new HashMap<>()).put(user, weight);
    }

    private void updateEventPairWeight(Long event, Long otherEvent, Long user, Double oldUserWight) {
        Long first = Math.min(event, otherEvent);
        Long second = Math.max(event, otherEvent);

        eventPairsWeight.putIfAbsent(first, new HashMap<>());
        eventPairsWeight.get(first).put(second,
                eventPairsWeight.get(first).getOrDefault(second, 0D)
                        - Math.min(oldUserWight, getEventUserWeight(otherEvent, user))
                        + Math.min(getEventUserWeight(event, user), getEventUserWeight(otherEvent, user)));
    }

    private Double calculateEventsScore(Long event, Long otherEvent) {
        Long first = Math.min(event, otherEvent);
        Long second = Math.max(event, otherEvent);

        Double a = eventPairsWeight.get(first).get(second);
        Double b = Math.sqrt(eventTotalWeight.get(first));
        Double c = Math.sqrt(eventTotalWeight.get(second));
        Double x = eventPairsWeight.get(first).get(second) / (Math.sqrt(eventTotalWeight.get(first)) * Math.sqrt(eventTotalWeight.get(second)));
        log.debug("---------");
        log.debug("события: first={}, second={}", first, second);
        log.debug("результат расчета похожести: min={}, norm1={}, norm2={}, res={}", a,b,c,x);

        return eventPairsWeight.get(first).get(second) / (Math.sqrt(eventTotalWeight.get(first)) * Math.sqrt(eventTotalWeight.get(second)));
    }
}