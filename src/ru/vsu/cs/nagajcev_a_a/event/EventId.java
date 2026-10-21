package ru.vsu.cs.nagajcev_a_a.event;

/**
 * Идентификатор события. Выдаётся автоматически при создании события и не меняется.
 * По нему определяется равенство событий.
 *
 * @param value положительный номер события
 */
public record EventId(long value) {
    /**
     * Проверяет номер события.
     *
     * @throws IllegalArgumentException если {@code value} не положительный
     */
    public EventId {
        if (value <= 0) throw new IllegalArgumentException("ID не может быть отрицательным");
    }
}
