package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import ru.vsu.cs.nagajcev_a_a.repeat.InvalidRepeatRuleException;
import ru.vsu.cs.nagajcev_a_a.repeat.NMonthRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.NYearRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;

import java.time.DateTimeException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DisplayName("NYearRepeat")
class NYearRepeatTest {

    private static final LocalDate START = LocalDate.of(2025, 3, 10);
    private static final LocalDate LEAP_DAY = LocalDate.of(2024, 2, 29);

    @Nested
    @Order(1)
    @DisplayName("Базовое поведение")
    class Basic {

        @Test
        @DisplayName("repeat = 0 возвращает саму начальную дату")
        void zeroRepeatReturnsStartDate() {
            RepeatRule rule = new NYearRepeat(1);

            assertEquals(START, rule.calculateNextDate(START, 0));
        }

        @Test
        @DisplayName("Ежегодный повтор сохраняет день и месяц")
        void yearlyKeepsDayAndMonth() {
            RepeatRule rule = new NYearRepeat(1);

            assertAll(
                    () -> assertEquals(LocalDate.of(2026, 3, 10), rule.calculateNextDate(START, 1)),
                    () -> assertEquals(LocalDate.of(2027, 3, 10), rule.calculateNextDate(START, 2)),
                    () -> assertEquals(LocalDate.of(2035, 3, 10), rule.calculateNextDate(START, 10))
            );
        }

        @Test
        @DisplayName("Интервал N лет: repeat = k добавляет k * N лет")
        void intervalMultipliesRepeat() {
            RepeatRule rule = new NYearRepeat(3);

            assertAll(
                    () -> assertEquals(LocalDate.of(2028, 3, 10), rule.calculateNextDate(START, 1)),
                    () -> assertEquals(LocalDate.of(2031, 3, 10), rule.calculateNextDate(START, 2)),
                    () -> assertEquals(LocalDate.of(2055, 3, 10), rule.calculateNextDate(START, 10))
            );
        }

        @Test
        @DisplayName("Ежегодный повтор совпадает с 12-м вхождением повтора по числу месяца")
        void yearlyMatchesEveryTwelfthMonthlyOccurrence() {
            RepeatRule yearly = new NYearRepeat(1);
            RepeatRule monthly = new NMonthRepeat(START.getDayOfMonth());

            for (int i = 0; i < 20; i++) {
                assertEquals(monthly.calculateNextDate(START, 12 * i), yearly.calculateNextDate(START, i),
                        "repeat #" + i);
            }
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Краевые случаи календаря")
    class CalendarEdges {

        @Test
        @DisplayName("29 февраля в невисокосном году попадает на 28 февраля")
        void leapDayInCommonYearClampsToFebruary28() {
            RepeatRule rule = new NYearRepeat(1);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 2, 28), rule.calculateNextDate(LEAP_DAY, 1)),
                    () -> assertEquals(LocalDate.of(2026, 2, 28), rule.calculateNextDate(LEAP_DAY, 2)),
                    () -> assertEquals(LocalDate.of(2027, 2, 28), rule.calculateNextDate(LEAP_DAY, 3))
            );
        }

        @Test
        @DisplayName("29 февраля возвращается в следующем високосном году, а не остаётся 28-м")
        void leapDayReturnsInNextLeapYear() {
            RepeatRule rule = new NYearRepeat(1);

            assertAll(
                    () -> assertEquals(LocalDate.of(2028, 2, 29), rule.calculateNextDate(LEAP_DAY, 4)),
                    () -> assertEquals(LocalDate.of(2032, 2, 29), rule.calculateNextDate(LEAP_DAY, 8))
            );
        }

        @Test
        @DisplayName("Раз в 4 года от 29 февраля — всегда 29 февраля")
        void everyFourYearsFromLeapDayStaysOnLeapDay() {
            RepeatRule rule = new NYearRepeat(4);

            for (int i = 0; i < 19; i++) {
                assertEquals(LocalDate.of(2024 + 4 * i, 2, 29), rule.calculateNextDate(LEAP_DAY, i), "repeat #" + i);
            }
        }

        @Test
        @DisplayName("2100 год не високосный: раз в 4 года от 29 февраля даёт 28 февраля 2100")
        void centuryYearIsNotLeap() {
            RepeatRule rule = new NYearRepeat(4);

            assertAll(
                    () -> assertEquals(LocalDate.of(2096, 2, 29), rule.calculateNextDate(LEAP_DAY, 18)),
                    () -> assertEquals(LocalDate.of(2100, 2, 28), rule.calculateNextDate(LEAP_DAY, 19)),
                    () -> assertEquals(LocalDate.of(2104, 2, 29), rule.calculateNextDate(LEAP_DAY, 20))
            );
        }

        @Test
        @DisplayName("2000 год високосный: раз в 100 лет от 29 февраля 1600 года")
        void yearDivisibleBy400IsLeap() {
            RepeatRule rule = new NYearRepeat(100);
            LocalDate leapDay1600 = LocalDate.of(1600, 2, 29);

            assertAll(
                    () -> assertEquals(LocalDate.of(1700, 2, 28), rule.calculateNextDate(leapDay1600, 1)),
                    () -> assertEquals(LocalDate.of(1900, 2, 28), rule.calculateNextDate(leapDay1600, 3)),
                    () -> assertEquals(LocalDate.of(2000, 2, 29), rule.calculateNextDate(leapDay1600, 4))
            );
        }

        @Test
        @DisplayName("28 февраля не сдвигается на 29-е в високосном году")
        void february28StaysFebruary28() {
            RepeatRule rule = new NYearRepeat(1);
            LocalDate february28 = LocalDate.of(2023, 2, 28);

            assertEquals(LocalDate.of(2024, 2, 28), rule.calculateNextDate(february28, 1));
        }

        @Test
        @DisplayName("1 марта остаётся 1 марта и в високосном, и в невисокосном году")
        void marchFirstIsStable() {
            RepeatRule rule = new NYearRepeat(1);
            LocalDate marchFirst = LocalDate.of(2023, 3, 1);

            assertAll(
                    () -> assertEquals(LocalDate.of(2024, 3, 1), rule.calculateNextDate(marchFirst, 1)),
                    () -> assertEquals(LocalDate.of(2025, 3, 1), rule.calculateNextDate(marchFirst, 2))
            );
        }

        @Test
        @DisplayName("31 декабря переходит на 31 декабря следующего года")
        void lastDayOfYear() {
            RepeatRule rule = new NYearRepeat(1);

            assertEquals(LocalDate.of(2026, 12, 31), rule.calculateNextDate(LocalDate.of(2025, 12, 31), 1));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Границы и исключения")
    class Boundaries {

        @Test
        @DisplayName("Произведение повтора на интервал больше int не переполняется")
        void noIntOverflow() {
            RepeatRule rule = new NYearRepeat(65_536);

            assertThrows(DateTimeException.class, () -> rule.calculateNextDate(START, 65_536));
        }

        @Test
        @DisplayName("Дата у верхней границы LocalDate ещё вычисляется")
        void reachesMaxYear() {
            RepeatRule rule = new NYearRepeat(1);
            LocalDate yearBeforeMax = LocalDate.MAX.minusYears(1);

            assertEquals(LocalDate.MAX, rule.calculateNextDate(yearBeforeMax, 1));
        }

        @Test
        @DisplayName("Выход за LocalDate.MAX выбрасывает DateTimeException")
        void pastMaxDateThrows() {
            RepeatRule rule = new NYearRepeat(1);

            assertThrows(DateTimeException.class, () -> rule.calculateNextDate(LocalDate.MAX, 1));
        }

        @Test
        @DisplayName("Неположительный интервал отклоняется")
        void nonPositiveIntervalRejected() {
            assertAll(
                    () -> assertThrows(InvalidRepeatRuleException.class, () -> new NYearRepeat(0)),
                    () -> assertThrows(InvalidRepeatRuleException.class, () -> new NYearRepeat(-1))
            );
        }

        @Test
        @DisplayName("Отрицательный repeat отклоняется")
        void negativeRepeatRejected() {
            RepeatRule rule = new NYearRepeat(1);

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> rule.calculateNextDate(START, -1)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> rule.calculateNextDate(START, Integer.MIN_VALUE))
            );
        }
    }
}
