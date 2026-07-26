package ru.practicum.explore_with_me.interaction_api.model.event;

public enum EventState {
    PENDING, PUBLISHED, CANCELED;

    @Override
    public String toString() {
        return name();
    }
}