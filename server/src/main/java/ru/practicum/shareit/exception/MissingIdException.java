package ru.practicum.shareit.exception;

public class MissingIdException extends RuntimeException {

    public MissingIdException(final String message) {
        super(message);
    }
}
