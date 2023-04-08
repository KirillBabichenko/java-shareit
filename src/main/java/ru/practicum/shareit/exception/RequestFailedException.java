package ru.practicum.shareit.exception;

public class RequestFailedException extends RuntimeException {

    public RequestFailedException(final String message) {
        super(message);
    }
}
