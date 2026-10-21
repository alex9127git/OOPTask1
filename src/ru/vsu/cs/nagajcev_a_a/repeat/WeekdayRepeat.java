package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;

/**
 * Еженедельное повторение по указанным дням недели, например «по понедельникам и четвергам».
 */
public class WeekdayRepeat implements RepeatRule {
    private final TreeSet<DayOfWeek> weekdays;

    /**
     * Создаёт правило с одним днём недели.
     *
     * @param weekday день недели
     * @throws NullPointerException если {@code weekday} равен {@code null}
     */
    public WeekdayRepeat(DayOfWeek weekday) {
        Objects.requireNonNull(weekday, "День недели не может быть равен null");
        this.weekdays = new TreeSet<>();
        this.weekdays.add(weekday);
    }

    /**
     * Создаёт правило с несколькими днями недели. Повторы в массиве игнорируются,
     * массив копируется, так что его последующее изменение не влияет на правило.
     *
     * @param weekdays дни недели
     * @throws NullPointerException       если массив или один из его элементов равен {@code null}
     * @throws InvalidRepeatRuleException если массив пустой
     */
    public WeekdayRepeat(DayOfWeek[] weekdays) {
        Objects.requireNonNull(weekdays, "Список дней недели не может быть равен null");
        if (weekdays.length == 0) throw new InvalidRepeatRuleException("Список дней недели не может быть пустым");
        for (DayOfWeek wd : weekdays) {
            Objects.requireNonNull(wd, "День недели не может быть равен null");
        }
        this.weekdays = new TreeSet<>(List.of(weekdays));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Первое вхождение — ближайший подходящий день недели, начиная с самой даты начала.
     */
    @Override
    public LocalDate calculateNextDate(LocalDate date, int repeat) {
        Objects.requireNonNull(date, "Дата не может быть равна null");
        if (repeat < 0) throw new IllegalArgumentException("Аргумент repeat не может быть отрицательным");

        DayOfWeek startWeekday = this.weekdays.ceiling(date.getDayOfWeek());
        if (startWeekday == null) startWeekday = this.weekdays.first();

        ArrayList<DayOfWeek> weekdayCycle = new ArrayList<>(this.weekdays);
        int index = weekdayCycle.indexOf(startWeekday);
        index = (int) ((index + (long) repeat) % weekdayCycle.size());

        int fullWeeks = repeat / weekdayCycle.size();
        LocalDate result = date.plusWeeks(fullWeeks);
        DayOfWeek weekday = weekdayCycle.get(index);

        int interval = weekday.getValue() - date.getDayOfWeek().getValue();
        if (interval < 0) interval += 7;
        return result.plusDays(interval);
    }
}
