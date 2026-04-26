package com.dochiri.habitservice.domain.record;

import com.dochiri.habitservice.domain.record.exception.InvalidHabitMemoException;

public record HabitMemo(
        String value
) {
    private static final int MAX_LENGTH = 200;
    private static final HabitMemo EMPTY = new HabitMemo(null);

    public HabitMemo {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new InvalidHabitMemoException(value.length());
        }
    }

    public static HabitMemo of(String value) {
        if (value == null) {
            return EMPTY;
        }
        return new HabitMemo(value);
    }

    public static HabitMemo empty() {
        return EMPTY;
    }

    public boolean isPresent() {
        return value != null;
    }
}
