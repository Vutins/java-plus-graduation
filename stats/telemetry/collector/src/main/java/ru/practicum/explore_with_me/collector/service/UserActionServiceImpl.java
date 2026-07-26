package ru.practicum.explore_with_me.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.explore_with_me.collector.producer.KafkaActionsProducer;
import ru.practicum.explore_with_me.collector.util.Utils;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {

    private final KafkaActionsProducer producer;

    @Override
    public void collectUserAction(UserActionProto event) {
        log.debug("building UserActionAvro: actionType={}", event.getActionType());
        UserActionAvro actionAvro = UserActionAvro.newBuilder()
                .setUserId(event.getUserId())
                .setEventId(event.getEventId())
                .setActionType(convertAction(event.getActionType()))
                .setTimestamp(Utils.timestampToInstant(event.getTimestamp()))
                .build();
        log.debug("sending event to Kafka");
        producer.send(actionAvro);
        producer.flush();
    }

    private ActionTypeAvro convertAction(ActionTypeProto action) {
        return switch (action) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> null;
        };
    }
}