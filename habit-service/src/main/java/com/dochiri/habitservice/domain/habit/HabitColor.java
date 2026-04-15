package com.dochiri.habitservice.domain.habit;

import com.dochiri.habitservice.domain.habit.exception.InvalidHabitColorException;

public record HabitColor(
        ColorType colorType
) {
    public HabitColor {
        if (colorType == null) {
            colorType = ColorType.GREEN;
        }
    }

    public static HabitColor of(ColorType colorType) {
        return new HabitColor(colorType);
    }

    public static HabitColor from(String color) {
        if (color == null || color.isBlank()) {
            return new HabitColor(ColorType.GREEN);
        }

        try {
            return new HabitColor(ColorType.valueOf(color));
        } catch (IllegalArgumentException e) {
            throw new InvalidHabitColorException(color);
        }
    }

    public String getHexValue() {
        return colorType.getHexValue();
    }
}
