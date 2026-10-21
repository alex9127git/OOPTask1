package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Повторение каждые N дней: вхождение с номером k приходится на дату начала плюс k·N дней.
 */
public class NDayRepeat implements RepeatRule {
    private final int daysInterval;

    /**
     * Создаёт правило «каждые {@code daysInterval} дней».
     *
     * @param daysInterval интервал между вхождениями в днях
     * @throws InvalidRepeatRuleException если интервал не положительный
     */
    public NDayRepeat(int daysInterval) {
        if (daysInterval <= 0) throw new InvalidRepeatRuleException("Интервал повтора должен быть положительным");
        this.daysInterval = daysInterval;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Первое вхождение совпадает с датой начала.
     */
    @Override
    public LocalDate calculateNextDate(LocalDate date, int repeat) {
        Objects.requireNonNull(date, "Дата не может быть равна null");
        if (repeat < 0) throw new IllegalArgumentException("Аргумент repeat не может быть отрицательным");
        return date.plusDays((long) repeat * daysInterval);
    }
}
