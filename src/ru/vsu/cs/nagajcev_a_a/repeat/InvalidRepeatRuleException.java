package ru.vsu.cs.nagajcev_a_a.repeat;

/**
 * Бросается при создании правила повторения с недопустимыми параметрами:
 * неположительным интервалом, несуществующим числом месяца, пустым списком дней недели.
 * <p>
 * Исключение непроверяемое: неверные параметры правила — ошибка в вызывающем коде.
 */
public class InvalidRepeatRuleException extends IllegalArgumentException {
    /**
     * Создаёт исключение с описанием ошибки.
     *
     * @param message описание недопустимого параметра
     */
    public InvalidRepeatRuleException(String message) {
        super(message);
    }
}
