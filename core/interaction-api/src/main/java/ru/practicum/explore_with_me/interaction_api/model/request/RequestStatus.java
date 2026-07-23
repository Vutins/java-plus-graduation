package ru.practicum.explore_with_me.interaction_api.model.request;

public enum RequestStatus {
    PENDING, CONFIRMED, REJECTED, CANCELED;

    @Override
    public String toString() {
        return name();
    }
}