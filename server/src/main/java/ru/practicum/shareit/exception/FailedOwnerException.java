package ru.practicum.shareit.exception;

public class FailedOwnerException extends RuntimeException {

    public FailedOwnerException(final String message) {
        super(message);
    }
}
