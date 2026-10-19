package ru.vsu.cs.nagajcev_a_a.repeat;

public enum CustomMonthRepeatPattern {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    LAST(-1),
    SECOND_TO_LAST(-2),
    THIRD_TO_LAST(-3),
    FOURTH_TO_LAST(-4);

    private final int value;

    CustomMonthRepeatPattern(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
