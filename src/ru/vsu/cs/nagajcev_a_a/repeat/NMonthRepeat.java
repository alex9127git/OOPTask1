package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;
import java.util.Objects;

public class NMonthRepeat implements RepeatRule {
    private final int dayNumber;

    public NMonthRepeat(int dayNumber) {
        if (dayNumber < 1 || dayNumber > 31)
            throw new InvalidRepeatRuleException(dayNumber + " не является корректным числом месяца");
        this.dayNumber = dayNumber;
    }

    @Override
    public LocalDate calculateNextDate(LocalDate date, int repeat) {
        Objects.requireNonNull(date, "Дата не может быть равна null");
        if (repeat < 0) throw new IllegalArgumentException("Аргумент repeat не может быть отрицательным");
        LocalDate result = date;

        if (date.getDayOfMonth() > Math.min(dayNumber, result.lengthOfMonth())) {
            result = result.plusMonths(1);
        }
        result = result.plusMonths(repeat);
        result = result.withDayOfMonth(Math.min(dayNumber, result.lengthOfMonth()));
        return result;
    }
}
