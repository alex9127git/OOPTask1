package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import ru.vsu.cs.nagajcev_a_a.endRule.AfterCount;
import ru.vsu.cs.nagajcev_a_a.event.Event;
import ru.vsu.cs.nagajcev_a_a.event.EventId;
import ru.vsu.cs.nagajcev_a_a.event.RepeatingEvent;
import ru.vsu.cs.nagajcev_a_a.event.SingleEvent;
import ru.vsu.cs.nagajcev_a_a.repeat.NDayRepeat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Событие — сущность: равенство определяется только неизменяемым идентификатором {@link EventId},
 * который выдаётся при создании. Поля и состояние серии на равенство не влияют.
 */
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class ContractTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 10);

    private static SingleEvent meeting() {
        return new SingleEvent("Встреча", "Планёрка", DATE);
    }

    private static RepeatingEvent weeklyMeeting() {
        return new RepeatingEvent("Встреча", "Планёрка", DATE, new NDayRepeat(7));
    }

    @Nested
    @Order(1)
    @DisplayName("EventId: контракт equals/hashCode")
    class EventIdContract {

        @Test
        @DisplayName("Рефлексивность")
        void reflexive() {
            EventId id = new EventId(7);

            assertEquals(id, id);
        }

        @Test
        @DisplayName("Симметричность и равные hashCode при одинаковом значении")
        void symmetricWithEqualHashCodes() {
            EventId first = new EventId(7);
            EventId second = new EventId(7);

            assertAll(
                    () -> assertEquals(first, second),
                    () -> assertEquals(second, first),
                    () -> assertEquals(first.hashCode(), second.hashCode())
            );
        }

        @Test
        @DisplayName("Транзитивность")
        void transitive() {
            EventId first = new EventId(7);
            EventId second = new EventId(7);
            EventId third = new EventId(7);

            assertAll(
                    () -> assertEquals(first, second),
                    () -> assertEquals(second, third),
                    () -> assertEquals(first, third)
            );
        }

        @Test
        @DisplayName("Разные значения — разные идентификаторы")
        void differentValuesAreNotEqual() {
            assertNotEquals(new EventId(7), new EventId(8));
        }

        @Test
        @DisplayName("Не равен null, числу с тем же значением и объекту другого типа")
        void notEqualToNullOrOtherType() {
            EventId id = new EventId(7);

            assertAll(
                    () -> assertNotEquals(null, id),
                    () -> assertNotEquals(7L, id),
                    () -> assertNotEquals("7", id)
            );
        }

        @Test
        @DisplayName("Работает как ключ HashMap")
        void worksAsHashMapKey() {
            Map<EventId, String> names = new HashMap<>();
            names.put(new EventId(7), "Планёрка");

            assertEquals("Планёрка", names.get(new EventId(7)));
        }

        @Test
        @DisplayName("Значение ≤ 0 — IllegalArgumentException")
        void nonPositiveValueThrows() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> new EventId(0)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new EventId(-1))
            );
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Идентификатор события")
    class EventIdentity {

        @Test
        @DisplayName("У нового события есть идентификатор")
        void newEventHasId() {
            assertAll(
                    () -> assertNotNull(meeting().getId()),
                    () -> assertNotNull(weeklyMeeting().getId())
            );
        }

        @Test
        @DisplayName("Идентификаторы уникальны для всех созданных событий обоих видов")
        void idsAreUnique() {
            Set<EventId> ids = new HashSet<>();
            for (int i = 0; i < 500; i++) {
                ids.add(meeting().getId());
                ids.add(weeklyMeeting().getId());
            }

            assertEquals(1000, ids.size());
        }

        @Test
        @DisplayName("getId возвращает один и тот же идентификатор")
        void idIsStable() {
            Event event = meeting();

            assertSame(event.getId(), event.getId());
        }

        @Test
        @DisplayName("Изменение полей не меняет идентификатор")
        void idSurvivesFieldChanges() {
            Event event = meeting();
            EventId before = event.getId();
            event.setName("Звонок");
            event.setDescription("Перенесли");
            event.setDate(DATE.plusDays(3));

            assertEquals(before, event.getId());
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Event: контракт equals/hashCode")
    class EventContract {

        @Test
        @DisplayName("Рефлексивность")
        void reflexive() {
            Event single = meeting();
            Event repeating = weeklyMeeting();

            assertAll(
                    () -> assertEquals(single, single),
                    () -> assertEquals(repeating, repeating)
            );
        }

        @Test
        @DisplayName("Два события с одинаковыми полями — разные события")
        void sameFieldsDifferentEvents() {
            Event first = meeting();
            Event second = meeting();

            assertAll(
                    () -> assertNotEquals(first, second),
                    () -> assertNotEquals(second, first)
            );
        }

        @Test
        @DisplayName("Две одинаковые серии — разные события")
        void sameSeriesDifferentEvents() {
            assertNotEquals(weeklyMeeting(), weeklyMeeting());
        }

        @Test
        @DisplayName("Одиночное и повторяющееся событие с одинаковыми полями не равны")
        void differentSubclassesAreNotEqual() {
            Event single = meeting();
            Event repeating = weeklyMeeting();

            assertAll(
                    () -> assertNotEquals(single, repeating),
                    () -> assertNotEquals(repeating, single)
            );
        }

        @Test
        @DisplayName("hashCode события совпадает с hashCode его идентификатора")
        void hashCodeComesFromId() {
            Event single = meeting();
            Event repeating = weeklyMeeting();

            assertAll(
                    () -> assertEquals(single.getId().hashCode(), single.hashCode()),
                    () -> assertEquals(repeating.getId().hashCode(), repeating.hashCode())
            );
        }

        @Test
        @DisplayName("Не равно null, своему идентификатору и объекту другого типа")
        void notEqualToNullOrOtherType() {
            Event event = meeting();

            assertAll(
                    () -> assertNotEquals(null, event),
                    () -> assertNotEquals(event.getId(), event),
                    () -> assertNotEquals("Встреча", event)
            );
        }

        @Test
        @DisplayName("Поля null не ломают equals и hashCode")
        void nullFieldsAreHandled() {
            Event first = new SingleEvent(null, null, DATE);
            Event second = new SingleEvent(null, null, DATE);

            assertAll(
                    () -> assertDoesNotThrow(first::hashCode),
                    () -> assertEquals(first, first),
                    () -> assertNotEquals(first, second)
            );
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Изменение события не ломает хеш-коллекции")
    class MutationSafety {

        @Test
        @DisplayName("Одиночное событие находится в HashSet после изменения всех полей")
        void singleEventStaysInHashSet() {
            SingleEvent event = meeting();
            Set<Event> events = new HashSet<>(Set.of(event));
            int hashBefore = event.hashCode();

            event.setName("Звонок");
            event.setDescription(null);
            event.setDate(DATE.plusYears(1));

            assertAll(
                    () -> assertEquals(hashBefore, event.hashCode()),
                    () -> assertTrue(events.contains(event)),
                    () -> assertTrue(events.remove(event))
            );
        }

        @Test
        @DisplayName("Серия находится в HashSet после переносов, удалений и смены правил")
        void repeatingEventStaysInHashSet() {
            RepeatingEvent event = weeklyMeeting();
            Set<Event> events = new HashSet<>(Set.of(event));
            int hashBefore = event.hashCode();

            event.addException(1, DATE.plusDays(9));
            event.deleteOccurrence(2);
            event.setEndRule(new AfterCount(10));
            event.setRepeatRule(new NDayRepeat(14));
            event.setName("Ретро");

            assertAll(
                    () -> assertEquals(hashBefore, event.hashCode()),
                    () -> assertTrue(events.contains(event)),
                    () -> assertTrue(events.remove(event))
            );
        }

        @Test
        @DisplayName("Событие остаётся ключом HashMap после изменения")
        void eventStaysHashMapKey() {
            RepeatingEvent event = weeklyMeeting();
            Map<Event, String> rooms = new HashMap<>();
            rooms.put(event, "Переговорная 3");

            event.setDate(DATE.plusDays(1));
            event.addException(2, DATE.plusDays(16));

            assertEquals("Переговорная 3", rooms.get(event));
        }

        @Test
        @DisplayName("Два события с одинаковыми полями — два элемента HashSet")
        void sameFieldsAreTwoSetElements() {
            Set<Event> events = new HashSet<>();
            events.add(meeting());
            events.add(meeting());

            assertEquals(2, events.size());
        }
    }
}
