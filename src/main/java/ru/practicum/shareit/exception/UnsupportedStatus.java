package ru.practicum.shareit.exception;

public class UnsupportedStatus extends RuntimeException {
    public UnsupportedStatus(final String message) {
        super(message);
    }
}
