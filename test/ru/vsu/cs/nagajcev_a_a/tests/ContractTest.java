package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import ru.vsu.cs.nagajcev_a_a.event.Event;
import ru.vsu.cs.nagajcev_a_a.event.RepeatingEvent;
import ru.vsu.cs.nagajcev_a_a.event.SingleEvent;
import ru.vsu.cs.nagajcev_a_a.repeat.NDayRepeat;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class ContractTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 10);

    private static Event meeting() {
        return new SingleEvent("Встреча", "Планёрка", DATE);
    }

    @Nested
    @Order(1)
    @DisplayName("Свойства отношения равенства")
    class EqualityProperties {

        @Test
        @DisplayName("Рефлексивность")
        void reflexive() {
            Event event = meeting();

            assertEquals(event, event);
        }

        @Test
        @DisplayName("Симметричность")
        void symmetric() {
            Event first = meeting();
            Event second = meeting();

            assertAll(
                    () -> assertEquals(first, second),
                    () -> assertEquals(second, first)
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
    }

    @Nested
    @Order(2)
    @DisplayName("Согласованность hashCode")
    class HashCodeConsistency {

        @Test
        @DisplayName("Равные события имеют равный hashCode")
        void equalEventsHaveEqualHashCodes() {
            Event first = meeting();
            Event second = meeting();

            assertEquals(first.hashCode(), second.hashCode());
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Что делает события неравными")
    class Inequality {

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
            Event repeating = new RepeatingEvent("Встреча", "Планёрка", DATE, new NDayRepeat(1));

            assertAll(
                    () -> assertNotEquals(single, repeating),
                    () -> assertNotEquals(repeating, single)
            );
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Поля null")
    class NullFields {

        @Test
        @DisplayName("Поля null не ломают equals и hashCode")
        void nullFieldsAreHandled() {
            Event first = new SingleEvent(null, null, LocalDate.of(2020, 1, 12));
            Event second = new SingleEvent(null, null, LocalDate.of(2020, 1, 12));

            assertAll(
                    () -> assertEquals(first, second),
                    () -> assertEquals(first.hashCode(), second.hashCode()),
                    () -> assertNotEquals(first, meeting())
            );
        }
    }
}
