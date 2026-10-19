package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.vsu.cs.nagajcev_a_a.event.Event;
import ru.vsu.cs.nagajcev_a_a.event.RepeatingEvent;
import ru.vsu.cs.nagajcev_a_a.event.SingleEvent;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Event: контракт equals/hashCode")
class EventTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 10);

    private static Event meeting() {
        return new SingleEvent("Встреча", "Планёрка", DATE);
    }

    @Test
    @DisplayName("Рефлексивность")
    void reflexive() {
        Event event = meeting();

        assertEquals(event, event);
    }

    @Test
    @DisplayName("Симметричность и равные hashCode у одинаковых событий")
    void symmetricWithEqualHashCodes() {
        Event first = meeting();
        Event second = meeting();

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(second, first),
                () -> assertEquals(first.hashCode(), second.hashCode())
        );
    }

    @Test
    @DisplayName("Транзитивность")
    void transitive() {
        Event first = meeting();
        Event second = meeting();
        Event third = meeting();

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(second, third),
                () -> assertEquals(first, third)
        );
    }

    @Test
    @DisplayName("Не равно null и объекту другого типа")
    void notEqualToNullOrOtherType() {
        Event event = meeting();

        assertAll(
                () -> assertNotEquals(null, event),
                () -> assertNotEquals("Встреча", event)
        );
    }

    @Test
    @DisplayName("Отличие в любом поле делает события неравными")
    void anyFieldDifferenceBreaksEquality() {
        Event event = meeting();

        assertAll(
                () -> assertNotEquals(new SingleEvent("Звонок", "Планёрка", DATE), event),
                () -> assertNotEquals(new SingleEvent("Встреча", "Ретро", DATE), event),
                () -> assertNotEquals(event, new SingleEvent("Встреча", "Планёрка", DATE.plusDays(1)))
        );
    }

    @Test
    @DisplayName("Одиночное и повторяющееся событие с одинаковыми полями не равны")
    void differentSubclassesAreNotEqual() {
        Event single = meeting();
        Event repeating = new RepeatingEvent("Встреча", "Планёрка", DATE);

        assertAll(
                () -> assertNotEquals(single, repeating),
                () -> assertNotEquals(repeating, single)
        );
    }

    @Test
    @DisplayName("Поля null не ломают equals и hashCode")
    void nullFieldsAreHandled() {
        Event first = new SingleEvent(null, null, null);
        Event second = new SingleEvent(null, null, null);

        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode()),
                () -> assertNotEquals(first, meeting())
        );
    }
}
