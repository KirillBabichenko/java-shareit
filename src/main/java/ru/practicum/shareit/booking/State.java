package ru.practicum.shareit.booking;

import ru.practicum.shareit.exception.UnsupportedStatus;

public enum State {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static State getStateFromText(String text) {
        for (State state : State.values()) {
            if (state.toString().equals(text)) {
                return state;
            }
        }
        throw new UnsupportedStatus("Unknown state: " + text);
    }
}
