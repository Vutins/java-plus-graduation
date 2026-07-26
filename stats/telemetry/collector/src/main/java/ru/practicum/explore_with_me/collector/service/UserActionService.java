package ru.practicum.explore_with_me.collector.service;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserActionService {
    void collectUserAction(UserActionProto event);
}