package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

public class CustomMonthRepeat implements RepeatRule {
    private final CustomMonthRepeatPattern pattern;
    private final DayOfWeek weekDay;

    public CustomMonthRepeat(CustomMonthRepeatPattern pattern, DayOfWeek weekDay) {
        Objects.requireNonNull(weekDay, "День недели не может быть равен null");
        Objects.requireNonNull(pattern, "Паттерн повтора не может быть равен null");
        this.pattern = pattern;
        this.weekDay = weekDay;
    }

    private LocalDate move(LocalDate date) {
        LocalDate result;
        int interval;
        if (pattern.getValue() > 0) {
            result = date.withDayOfMonth(1);
            interval = this.weekDay.getValue() - result.getDayOfWeek().getValue();
            if (interval < 0) interval += 7;
            result = result.plusDays(interval);
            result = result.plusWeeks(pattern.getValue() - 1);
        } else {
            result = date.withDayOfMonth(date.lengthOfMonth());
            interval = result.getDayOfWeek().getValue() - this.weekDay.getValue();
            if (interval < 0) interval += 7;
            result = result.plusDays(-interval);
            result = result.plusWeeks(pattern.getValue() + 1);
        }
        return result;
    }

    @Override
    public LocalDate calculateNextDate(LocalDate date, int repeat) {
        Objects.requireNonNull(date, "Дата не может быть равна null");
        if (repeat < 0) throw new IllegalArgumentException("Аргумент repeat не может быть отрицательным");
        
        LocalDate result = date;
        if (move(date).isBefore(date)) {
            result = result.plusMonths(1);
        }
        result = result.plusMonths(repeat);
        result = move(result);
        return result;
    }
}
