package com.dochiri.habitservice.domain;

public record HabitColor(
        ColorType colorType
) {
    public enum ColorType {
        BLUE("#3b82f6"),
        GREEN("#10b981"),
        RED("#ef4444"),
        YELLOW("#f59e0b"),
        PURPLE("#8b5cf6"),
        PINK("#ec4899");

        private final String hexValue;

        ColorType(String hexValue) {
            this.hexValue = hexValue;
        }

        public String getHexValue() {
            return hexValue;
        }
    }

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