package ru.vsu.cs.nagajcev_a_a.endRule;

import java.time.LocalDate;

/**
 * Серия заканчивается в заданную дату включительно.
 * <p>
 * Сравнивается исходная дата вхождения, а не дата после переноса: последнее вхождение
 * можно перенести и за конечную дату.
 *
 * @param endDate последняя дата, на которую ещё может прийтись вхождение
 */
public record Until(LocalDate endDate) implements EndRule {
    /**
     * Проверяет конечную дату.
     *
     * @throws NullPointerException если {@code endDate} равна {@code null}
     */
    public Until {
        if (endDate == null) throw new NullPointerException("Дата не может быть равна null");
    }
}
