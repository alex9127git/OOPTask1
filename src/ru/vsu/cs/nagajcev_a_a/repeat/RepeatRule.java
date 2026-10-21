package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;

/**
 * Правило повторения события: по дате начала серии вычисляет дату любого её вхождения.
 * <p>
 * Вхождения нумеруются с нуля и всегда отсчитываются от начала серии. Вычислять следующее вхождение
 * от предыдущего нельзя: например, ежегодное правило от 29 февраля через невисокосный год
 * навсегда «съехало» бы на 28-е.
 */
public interface RepeatRule {
    /**
     * Вычисляет дату вхождения с номером {@code repeat} для серии, начинающейся с {@code date}.
     * Вхождение с номером 0 — первая подходящая под правило дата, не раньше {@code date}.
     *
     * @param date   дата начала серии
     * @param repeat номер вхождения, начиная с 0
     * @return дата вхождения; для {@code repeat > 0} строго позже предыдущего вхождения
     * @throws NullPointerException если {@code date} равна {@code null}
     * @throws IllegalArgumentException если {@code repeat} отрицательный
     */
    LocalDate calculateNextDate(LocalDate date, int repeat);
}
