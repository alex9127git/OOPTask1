package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import ru.vsu.cs.nagajcev_a_a.repeat.InvalidRepeatRuleException;
import ru.vsu.cs.nagajcev_a_a.repeat.NMonthRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DisplayName("NMonthRepeat")
class NMonthRepeatTest {

    private static final LocalDate START = LocalDate.of(2025, 3, 10);

    @Nested
    @Order(1)
    @DisplayName("Базовое поведение")
    class Basic {

        @Test
        @DisplayName("repeat = 0 до нужного числа сдвигает дату вперёд в том же месяце")
        void zeroRepeatBeforeDayStaysInMonth() {
            RepeatRule rule = new NMonthRepeat(15);

            assertEquals(LocalDate.of(2025, 3, 15), rule.calculateNextDate(START, 0));
        }

        @Test
        @DisplayName("repeat = 0 в нужное число возвращает саму начальную дату")
        void zeroRepeatOnDayReturnsStartDate() {
            RepeatRule rule = new NMonthRepeat(10);

            assertEquals(START, rule.calculateNextDate(START, 0));
        }

        @Test
        @DisplayName("repeat = 0 после нужного числа переходит на следующий месяц")
        void zeroRepeatAfterDayMovesToNextMonth() {
            RepeatRule rule = new NMonthRepeat(5);

            assertEquals(LocalDate.of(2025, 4, 5), rule.calculateNextDate(START, 0));
        }

        @Test
        @DisplayName("repeat = N добавляет N месяцев")
        void nthRepeatAddsNMonths() {
            RepeatRule rule = new NMonthRepeat(15);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 4, 15), rule.calculateNextDate(START, 1)),
                    () -> assertEquals(LocalDate.of(2025, 5, 15), rule.calculateNextDate(START, 2)),
                    () -> assertEquals(LocalDate.of(2026, 3, 15), rule.calculateNextDate(START, 12))
            );
        }

        @Test
        @DisplayName("Первое число месяца")
        void firstDayOfMonth() {
            RepeatRule rule = new NMonthRepeat(1);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 4, 1), rule.calculateNextDate(START, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 1), rule.calculateNextDate(LocalDate.of(2025, 3, 1), 0)),
                    () -> assertEquals(LocalDate.of(2025, 5, 1), rule.calculateNextDate(START, 1))
            );
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Краевые случаи календаря")
    class CalendarEdges {

        @Test
        @DisplayName("31-е число в месяцах без 31-го попадает на последний день месяца")
        void thirtyFirstClampsToLastDayOfMonth() {
            RepeatRule rule = new NMonthRepeat(31);
            LocalDate january = LocalDate.of(2025, 1, 1);
            LocalDate[] expected = {
                    LocalDate.of(2025, 1, 31), LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 31),
                    LocalDate.of(2025, 4, 30), LocalDate.of(2025, 5, 31), LocalDate.of(2025, 6, 30),
                    LocalDate.of(2025, 7, 31), LocalDate.of(2025, 8, 31), LocalDate.of(2025, 9, 30),
                    LocalDate.of(2025, 10, 31), LocalDate.of(2025, 11, 30), LocalDate.of(2025, 12, 31)
            };

            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], rule.calculateNextDate(january, i), "repeat #" + i);
            }
        }

        @Test
        @DisplayName("Начальная дата в месяце без 31-го: вхождение в последний день этого же месяца")
        void startInShortMonthStaysInThatMonth() {
            RepeatRule rule = new NMonthRepeat(31);
            LocalDate april = LocalDate.of(2025, 4, 5);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 4, 30), rule.calculateNextDate(april, 0)),
                    () -> assertEquals(LocalDate.of(2025, 5, 31), rule.calculateNextDate(april, 1))
            );
        }

        @Test
        @DisplayName("Начальная дата — последний день короткого месяца, он и есть вхождение")
        void startOnClampedDayIsOccurrence() {
            RepeatRule rule = new NMonthRepeat(31);
            LocalDate lastOfApril = LocalDate.of(2025, 4, 30);

            assertAll(
                    () -> assertEquals(lastOfApril, rule.calculateNextDate(lastOfApril, 0)),
                    () -> assertEquals(LocalDate.of(2025, 5, 31), rule.calculateNextDate(lastOfApril, 1))
            );
        }

        @Test
        @DisplayName("После 31 января 30-е число попадает на 28 февраля, а не на март")
        void thirtiethAfterJanuaryEndLandsOnFebruaryEnd() {
            RepeatRule rule = new NMonthRepeat(30);
            LocalDate endOfJanuary = LocalDate.of(2025, 1, 31);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 2, 28), rule.calculateNextDate(endOfJanuary, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 30), rule.calculateNextDate(endOfJanuary, 1))
            );
        }

        @Test
        @DisplayName("После усечения до конца февраля число возвращается к исходному")
        void dayRecoversAfterClamping() {
            RepeatRule rule = new NMonthRepeat(30);
            LocalDate endOfFebruary = LocalDate.of(2025, 2, 28);

            assertAll(
                    () -> assertEquals(endOfFebruary, rule.calculateNextDate(endOfFebruary, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 30), rule.calculateNextDate(endOfFebruary, 1)),
                    () -> assertEquals(LocalDate.of(2025, 4, 30), rule.calculateNextDate(endOfFebruary, 2))
            );
        }

        @Test
        @DisplayName("29-е число в високосном феврале — 29 февраля")
        void twentyNinthInLeapFebruary() {
            RepeatRule rule = new NMonthRepeat(29);

            assertAll(
                    () -> assertEquals(LocalDate.of(2024, 2, 29), rule.calculateNextDate(LocalDate.of(2024, 2, 10), 0)),
                    () -> assertEquals(LocalDate.of(2024, 3, 29), rule.calculateNextDate(LocalDate.of(2024, 2, 10), 1))
            );
        }

        @Test
        @DisplayName("29-е число в невисокосном феврале — 28 февраля")
        void twentyNinthInCommonFebruary() {
            RepeatRule rule = new NMonthRepeat(29);
            LocalDate start = LocalDate.of(2025, 2, 10);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 2, 28), rule.calculateNextDate(start, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 29), rule.calculateNextDate(start, 1))
            );
        }

        @Test
        @DisplayName("29-е число через несколько лет попадает и на 29, и на 28 февраля")
        void twentyNinthAcrossYears() {
            RepeatRule rule = new NMonthRepeat(29);
            LocalDate start = LocalDate.of(2024, 1, 1);

            assertAll(
                    () -> assertEquals(LocalDate.of(2024, 2, 29), rule.calculateNextDate(start, 1)),
                    () -> assertEquals(LocalDate.of(2025, 2, 28), rule.calculateNextDate(start, 13)),
                    () -> assertEquals(LocalDate.of(2028, 2, 29), rule.calculateNextDate(start, 49))
            );
        }

        @Test
        @DisplayName("Переход через конец года")
        void crossesYearEnd() {
            RepeatRule rule = new NMonthRepeat(5);

            assertAll(
                    () -> assertEquals(LocalDate.of(2026, 1, 5), rule.calculateNextDate(LocalDate.of(2025, 12, 20), 0)),
                    () -> assertEquals(LocalDate.of(2026, 2, 5), rule.calculateNextDate(LocalDate.of(2025, 12, 20), 1))
            );
        }

        @Test
        @DisplayName("Переход через конец года с усечением: 31 декабря -> 28 февраля")
        void crossesYearEndWithClamping() {
            RepeatRule rule = new NMonthRepeat(31);

            assertEquals(LocalDate.of(2025, 2, 28), rule.calculateNextDate(LocalDate.of(2024, 12, 31), 2));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Общие свойства")
    class Properties {

        @Test
        @DisplayName("Для любого числа и любой начальной даты года вхождения идут по месяцам подряд без пропусков")
        void occurrencesFollowConsecutiveMonths() {
            for (int dayNumber = 1; dayNumber <= 31; dayNumber++) {
                RepeatRule rule = new NMonthRepeat(dayNumber);

                for (LocalDate start = LocalDate.of(2024, 1, 1); start.getYear() == 2024; start = start.plusDays(1)) {
                    LocalDate first = rule.calculateNextDate(start, 0);
                    String context = "day " + dayNumber + ", start " + start;

                    assertTrue(!first.isBefore(start), context + " -> " + first);
                    YearMonth firstMonth = YearMonth.from(first);
                    assertTrue(firstMonth.equals(YearMonth.from(start))
                            || firstMonth.equals(YearMonth.from(start).plusMonths(1)), context + " -> " + first);

                    for (int i = 0; i < 3; i++) {
                        YearMonth month = firstMonth.plusMonths(i);
                        LocalDate expected = month.atDay(Math.min(dayNumber, month.lengthOfMonth()));

                        assertEquals(expected, rule.calculateNextDate(start, i), context + ", repeat #" + i);
                    }
                }
            }
        }

        @Test
        @DisplayName("Шаг на repeat + 1 совпадает с шагом на 1 от вхождения номер repeat")
        void stepFromOccurrenceMatchesStepFromStart() {
            RepeatRule rule = new NMonthRepeat(31);

            for (LocalDate start = LocalDate.of(2024, 1, 1); start.getYear() == 2024; start = start.plusDays(5)) {
                for (int i = 0; i < 14; i++) {
                    LocalDate occurrence = rule.calculateNextDate(start, i);

                    assertEquals(rule.calculateNextDate(start, i + 1), rule.calculateNextDate(occurrence, 1),
                            "start " + start + ", repeat #" + i);
                }
            }
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Границы и исключения")
    class Boundaries {

        @Test
        @DisplayName("Большой repeat: 1200 месяцев — ровно 100 лет")
        void largeRepeatCountsWholeYears() {
            RepeatRule rule = new NMonthRepeat(31);

            assertEquals(LocalDate.of(2125, 1, 31), rule.calculateNextDate(LocalDate.of(2025, 1, 1), 1200));
        }

        @Test
        @DisplayName("repeat = Integer.MAX_VALUE не переполняется")
        void maxRepeatDoesNotOverflow() {
            RepeatRule rule = new NMonthRepeat(15);
            LocalDate first = LocalDate.of(2025, 3, 15);

            assertEquals(first.plusMonths(Integer.MAX_VALUE), rule.calculateNextDate(START, Integer.MAX_VALUE));
        }

        @Test
        @DisplayName("Выход за LocalDate.MAX выбрасывает DateTimeException")
        void pastMaxDateThrows() {
            RepeatRule rule = new NMonthRepeat(1);

            assertThrows(DateTimeException.class, () -> rule.calculateNextDate(LocalDate.MAX, 0));
        }

        @Test
        @DisplayName("Число месяца вне 1..31 отклоняется")
        void dayNumberOutOfRangeRejected() {
            assertAll(
                    () -> assertThrows(InvalidRepeatRuleException.class, () -> new NMonthRepeat(0)),
                    () -> assertThrows(InvalidRepeatRuleException.class, () -> new NMonthRepeat(32)),
                    () -> assertThrows(InvalidRepeatRuleException.class, () -> new NMonthRepeat(-1))
            );
        }

        @Test
        @DisplayName("Отрицательный repeat отклоняется")
        void negativeRepeatRejected() {
            RepeatRule rule = new NMonthRepeat(15);

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> rule.calculateNextDate(START, -1)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> rule.calculateNextDate(START, Integer.MIN_VALUE))
            );
        }
    }
}
