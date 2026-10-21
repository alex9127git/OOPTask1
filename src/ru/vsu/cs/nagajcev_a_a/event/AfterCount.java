package ru.vsu.cs.nagajcev_a_a.event;

/**
 * Серия заканчивается после заданного числа вхождений.
 * <p>
 * Вхождения считаются по номерам: существуют вхождения с номерами от 0 до {@code count - 1}.
 * Перенос не продлевает серию, а удалённое вхождение тоже расходует счётчик.
 *
 * @param count число вхождений серии
 */
public record AfterCount(int count) implements EndRule {
    /**
     * Проверяет число вхождений.
     *
     * @throws IllegalArgumentException если {@code count} не положительный
     */
    public AfterCount {
        if (count <= 0)
            throw new IllegalArgumentException("Количество повторений в серии не может быть меньше или равно нулю");
    }
}
