package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;

public class NDayRepeat implements RepeatRule {
    private final int daysInterval;

    public NDayRepeat(int daysInterval) {
        if (daysInterval <= 0) throw new InvalidRepeatRuleException("Интервал повтора должен быть положительным");
        this.daysInterval = daysInterval;
    }

    @Override
    public LocalDate calculateNextDate(LocalDate date, int repeat) {
        return date.plusDays((long) repeat * daysInterval);
    }
}
