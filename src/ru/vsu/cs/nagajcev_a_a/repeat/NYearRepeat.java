package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Повторение раз в N лет в тот же день и месяц, что и дата начала.
 * <p>
 * Серия от 29 февраля в невисокосные годы приходится на 28 февраля, а в високосные — снова на 29-е.
 */
public class NYearRepeat implements RepeatRule {
    private final int yearsInterval;

    /**
     * Создаёт правило «раз в {@code yearsInterval} лет».
     *
     * @param yearsInterval интервал между вхождениями в годах
     * @throws InvalidRepeatRuleException если интервал не положительный
     */
    public NYearRepeat(int yearsInterval) {
        if (yearsInterval <= 0) throw new InvalidRepeatRuleException("Интервал повтора должен быть положительным");
        this.yearsInterval = yearsInterval;
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
        return date.plusYears((long) repeat * yearsInterval);
    }
}
