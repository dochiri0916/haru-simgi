package com.dochiri.habitservice.domain.habit;

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

    public static HabitColor ofDefault() {
        return new HabitColor(ColorType.GREEN);
    }

    public static HabitColor from(HabitColor color) {
        return color == null ? new HabitColor(ColorType.GREEN) : color;
    }

    public String getHexValue() {
        return colorType.getHexValue();
    }
}