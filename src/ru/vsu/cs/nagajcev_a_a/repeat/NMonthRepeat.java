package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Ежемесячное повторение в заданное число месяца.
 * <p>
 * Если в месяце нет такого числа (31-е в апреле, 30-е в феврале), вхождение переносится на последний день
 * месяца, а в следующем месяце снова приходится на исходное число.
 */
public class NMonthRepeat implements RepeatRule {
    private final int dayNumber;

    /**
     * Создаёт правило «каждое {@code dayNumber}-е число месяца».
     *
     * @param dayNumber число месяца от 1 до 31
     * @throws InvalidRepeatRuleException если число вне диапазона 1..31
     */
    public NMonthRepeat(int dayNumber) {
        if (dayNumber < 1 || dayNumber > 31)
            throw new InvalidRepeatRuleException(dayNumber + " не является корректным числом месяца");
        this.dayNumber = dayNumber;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Первое вхождение — ближайшее нужное число, начиная с самой даты начала.
     */
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
