package ru.practicum.shareit.exception;

public class MissingValidationException extends RuntimeException {

    public MissingValidationException(final String message) {
        super(message);
    }
}
