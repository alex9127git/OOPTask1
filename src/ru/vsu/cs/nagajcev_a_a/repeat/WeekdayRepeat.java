package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;

public class WeekdayRepeat implements RepeatRule {
    private final TreeSet<DayOfWeek> weekdays;

    public WeekdayRepeat(DayOfWeek weekday) {
        if (weekday == null) throw new InvalidRepeatRuleException("День недели не может быть равен null");
        this.weekdays = new TreeSet<>();
        this.weekdays.add(weekday);
    }

    public WeekdayRepeat(DayOfWeek[] weekdays) {
        if (weekdays == null) throw new InvalidRepeatRuleException("Список дней недели не может быть равен null");
        if (weekdays.length == 0) throw new InvalidRepeatRuleException("Список дней недели не может быть пустым");
        for (DayOfWeek wd : weekdays) {
            if (wd == null) {
                throw new InvalidRepeatRuleException("День недели не может быть равен null");
            }
        }
        this.weekdays = new TreeSet<>(List.of(weekdays));
    }

    @Override
    public LocalDate calculateNextDate(LocalDate date, int repeat) {
        DayOfWeek startWeekday = this.weekdays.ceiling(date.getDayOfWeek());
        ArrayList<DayOfWeek> weekdayCycle = new ArrayList<>(this.weekdays);
        int index = weekdayCycle.indexOf(startWeekday);
        index = (index + repeat) % weekdayCycle.size();
        int fullWeeks = repeat / weekdayCycle.size();
        LocalDate result = date.plusWeeks(fullWeeks);
        DayOfWeek weekday = weekdayCycle.get(index);
        int interval = weekday.getValue() - date.getDayOfWeek().getValue();
        if (interval < 0) interval += 7;
        return result.plusDays(interval);
    }
}
