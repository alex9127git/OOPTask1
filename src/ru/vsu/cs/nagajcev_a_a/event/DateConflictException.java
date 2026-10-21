package ru.vsu.cs.nagajcev_a_a.event;

/**
 * Бросается, когда операция над серией поставила бы два вхождения на одну дату:
 * перенос на занятую дату, отмена переноса или восстановление удалённого вхождения,
 * когда его исходная дата уже занята.
 */
public class DateConflictException extends RuntimeException {
    /**
     * Создаёт исключение с описанием конфликта.
     *
     * @param message описание конфликта
     */
    public DateConflictException(String message) {
        super(message);
    }
}
