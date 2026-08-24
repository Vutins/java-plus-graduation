package ru.practicum.explore_with_me.analyzer.service.action;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionsService {
    void updateUserActionInteractions(UserActionAvro action);
}