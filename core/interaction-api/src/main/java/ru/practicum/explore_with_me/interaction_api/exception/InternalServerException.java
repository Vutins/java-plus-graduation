package ru.practicum.explore_with_me.interaction_api.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }
}
