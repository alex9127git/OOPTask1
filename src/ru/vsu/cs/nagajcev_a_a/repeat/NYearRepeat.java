package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;

public class NYearRepeat implements RepeatRule {
    private final int yearsInterval;

    public NYearRepeat(int yearsInterval) {
        if (yearsInterval <= 0) throw new InvalidRepeatRuleException("Интервал повтора должен быть положительным");
        this.yearsInterval = yearsInterval;
    }

    @Override
    public LocalDate calculateNextDate(LocalDate date, int repeat) {
        if (repeat < 0) throw new IllegalArgumentException("Аргумент repeat не может быть отрицательным");
        return date.plusYears((long) repeat * yearsInterval);
    }
}
