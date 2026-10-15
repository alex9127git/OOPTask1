package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import ru.vsu.cs.nagajcev_a_a.repeat.NDayRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;

import java.time.DateTimeException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DisplayName("NDayRepeat")
class NDayRepeatTest {

    private static final LocalDate START = LocalDate.of(2025, 3, 10);

    @Nested
    @Order(1)
    @DisplayName("Базовое поведение")
    class Basic {

        @Test
        @DisplayName("repeat = 0 возвращает саму начальную дату")
        void zeroRepeatReturnsStartDate() {
            RepeatRule rule = new NDayRepeat(5);

            assertEquals(START, rule.calculateNextDate(START, 0));
        }

        @Test
        @DisplayName("repeat = 1 добавляет ровно один интервал")
        void firstRepeatAddsOneInterval() {
            RepeatRule rule = new NDayRepeat(5);

            assertEquals(LocalDate.of(2025, 3, 15), rule.calculateNextDate(START, 1));
        }

        @Test
        @DisplayName("repeat = N добавляет N интервалов")
        void nthRepeatAddsNIntervals() {
            RepeatRule rule = new NDayRepeat(3);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 3, 13), rule.calculateNextDate(START, 1)),
                    () -> assertEquals(LocalDate.of(2025, 3, 16), rule.calculateNextDate(START, 2)),
                    () -> assertEquals(LocalDate.of(2025, 3, 19), rule.calculateNextDate(START, 3))
            );
        }

        @Test
        @DisplayName("Интервал в 7 дней всегда должен попадать на тот же день недели")
        void weeklyIntervalKeepsWeekday() {
            RepeatRule rule = new NDayRepeat(7);

            for (int i = 0; i < 60; i++) {
                assertEquals(START.getDayOfWeek(), rule.calculateNextDate(START, i).getDayOfWeek(),
                        "repeat #" + i);
            }
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Краевые случаи календаря")
    class CalendarEdges {

        @Test
        @DisplayName("Переход через конец месяца")
        void crossesMonthEnd() {
            RepeatRule rule = new NDayRepeat(3);

            assertEquals(LocalDate.of(2025, 2, 2), rule.calculateNextDate(LocalDate.of(2025, 1, 30), 1));
        }

        @Test
        @DisplayName("Переход через конец года")
        void crossesYearEnd() {
            RepeatRule rule = new NDayRepeat(2);

            assertEquals(LocalDate.of(2026, 1, 1), rule.calculateNextDate(LocalDate.of(2025, 12, 30), 1));
        }

        @Test
        @DisplayName("Попадает на 29 февраля в високосном году")
        void landsOnLeapDay() {
            RepeatRule rule = new NDayRepeat(1);

            assertEquals(LocalDate.of(2024, 2, 29), rule.calculateNextDate(LocalDate.of(2024, 2, 28), 1));
        }

        @Test
        @DisplayName("В невисокосном году переходит сразу на 1 марта")
        void skipsLeapDayInCommonYear() {
            RepeatRule rule = new NDayRepeat(1);

            assertEquals(LocalDate.of(2023, 3, 1), rule.calculateNextDate(LocalDate.of(2023, 2, 28), 1));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Границы и исключения")
    class Boundaries {

        @Test
        @DisplayName("Произведение повтора на интервал больше int не переполняется")
        void noIntOverflow() {
            RepeatRule rule = new NDayRepeat(100_000);

            assertEquals(START.plusDays(10_000_000_000L), rule.calculateNextDate(START, 100_000));
        }

        @Test
        @DisplayName("Выход за LocalDate.MAX выбрасывает DateTimeException")
        void pastMaxDateThrows() {
            RepeatRule rule = new NDayRepeat(1);

            assertThrows(DateTimeException.class, () -> rule.calculateNextDate(LocalDate.MAX, 1));
        }

        @Test
        @DisplayName("Неположительный интервал отклоняется")
        void nonPositiveIntervalRejected() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> new NDayRepeat(0)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new NDayRepeat(-1))
            );
        }
    }
}
