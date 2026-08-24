package ru.practicum.explore_with_me.analyzer.service.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.analyzer.model.dao.Interaction;
import ru.practicum.explore_with_me.analyzer.model.repository.InteractionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserActionsServiceImpl implements UserActionsService {

    private final InteractionRepository interactionRepository;

    @Override
    public void updateUserActionInteractions(UserActionAvro action) {
        Optional<Interaction> interactionOpt = interactionRepository.findByEventIdAndUserId(action.getEventId(), action.getUserId());
        Interaction interaction;
        if (interactionOpt.isPresent()) {
            interaction = interactionOpt.get();

            if (getActionWeight(action.getActionType()) <= interaction.getRating()) {
                return;
            }

            interaction.setRating(getActionWeight(action.getActionType()));
        } else {
            interaction = Interaction.builder()
                    .userId(action.getUserId())
                    .eventId(action.getEventId())
                    .rating(getActionWeight(action.getActionType()))
                    .ts(LocalDateTime.ofInstant(action.getTimestamp(), ZoneId.systemDefault()))
                    .build();
        }
        interactionRepository.save(interaction);
    }

    private Double getActionWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4D;
            case REGISTER -> 0.8D;
            case LIKE -> 1D;
        };
    }
}