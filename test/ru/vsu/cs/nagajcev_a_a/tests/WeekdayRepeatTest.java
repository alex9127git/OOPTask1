package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import ru.vsu.cs.nagajcev_a_a.repeat.InvalidRepeatRuleException;
import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;
import ru.vsu.cs.nagajcev_a_a.repeat.WeekdayRepeat;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DisplayName("WeekdayRepeat")
class WeekdayRepeatTest {

    private static final LocalDate START = LocalDate.of(2025, 3, 10);

    @Nested
    @Order(1)
    @DisplayName("Базовое поведение")
    class Basic {

        @Test
        @DisplayName("repeat = 0 в нужный день недели возвращает саму начальную дату")
        void zeroRepeatOnMatchingWeekdayReturnsStartDate() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.MONDAY);

            assertEquals(START, rule.calculateNextDate(START, 0));
        }

        @Test
        @DisplayName("repeat = 0 сдвигает дату вперёд до ближайшего нужного дня недели")
        void zeroRepeatMovesToNearestWeekday() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.THURSDAY);

            assertEquals(LocalDate.of(2025, 3, 13), rule.calculateNextDate(START, 0));
        }

        @Test
        @DisplayName("repeat = 1 добавляет ровно одну неделю")
        void firstRepeatAddsOneWeek() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.MONDAY);

            assertEquals(LocalDate.of(2025, 3, 17), rule.calculateNextDate(START, 1));
        }

        @Test
        @DisplayName("repeat = N добавляет N недель")
        void nthRepeatAddsNWeeks() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.FRIDAY);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 3, 14), rule.calculateNextDate(START, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 21), rule.calculateNextDate(START, 1)),
                    () -> assertEquals(LocalDate.of(2025, 3, 28), rule.calculateNextDate(START, 2))
            );
        }

        @Test
        @DisplayName("Для любого дня недели результат попадает на этот день и не дальше чем через 6 дней")
        void everyWeekdayLandsWithinOneWeek() {
            for (DayOfWeek weekday : DayOfWeek.values()) {
                RepeatRule rule = new WeekdayRepeat(weekday);
                LocalDate result = rule.calculateNextDate(START, 0);

                assertEquals(weekday, result.getDayOfWeek(), "weekday " + weekday);
                assertTrue(!result.isBefore(START) && !result.isAfter(START.plusDays(6)),
                        "weekday " + weekday + " -> " + result);
            }
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Краевые случаи календаря")
    class CalendarEdges {

        @Test
        @DisplayName("Воскресенье -> понедельник: сдвиг на 1 день через границу недели")
        void sundayToMonday() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.MONDAY);

            LocalDate sunday = LocalDate.of(2025, 3, 9);

            assertEquals(LocalDate.of(2025, 3, 10), rule.calculateNextDate(sunday, 0));
        }

        @Test
        @DisplayName("Понедельник -> воскресенье: сдвиг на 6 дней")
        void mondayToSunday() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.SUNDAY);

            assertEquals(LocalDate.of(2025, 3, 16), rule.calculateNextDate(START, 0));
        }

        @Test
        @DisplayName("Переход через конец года")
        void crossesYearEnd() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.FRIDAY);

            assertEquals(LocalDate.of(2026, 1, 2), rule.calculateNextDate(LocalDate.of(2025, 12, 30), 0));
        }

        @Test
        @DisplayName("Попадает на 29 февраля в високосном году")
        void landsOnLeapDay() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.THURSDAY);

            assertEquals(LocalDate.of(2024, 2, 29), rule.calculateNextDate(LocalDate.of(2024, 2, 26), 0));
        }

        @Test
        @DisplayName("В невисокосном году переходит сразу на 2 марта")
        void skipsLeapDayInCommonYear() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.THURSDAY);

            assertEquals(LocalDate.of(2025, 3, 6), rule.calculateNextDate(LocalDate.of(2025, 2, 27), 1));
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Границы и исключения")
    class Boundaries {

        @Test
        @DisplayName("Выход за LocalDate.MAX выбрасывает DateTimeException")
        void pastMaxDateThrows() {
            RepeatRule rule = new WeekdayRepeat(LocalDate.MAX.getDayOfWeek().plus(1));

            assertThrows(DateTimeException.class, () -> rule.calculateNextDate(LocalDate.MAX, 0));
        }

        @Test
        @DisplayName("День недели null отклоняется")
        void nullWeekdayRejected() {
            assertThrows(IllegalArgumentException.class, () -> new WeekdayRepeat((DayOfWeek) null));
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Несколько дней недели")
    class MultipleWeekdays {

        @Test
        @DisplayName("repeat считает вхождения, а не недели")
        void repeatCountsOccurrences() {
            RepeatRule rule = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY});

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 3, 10), rule.calculateNextDate(START, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 12), rule.calculateNextDate(START, 1)),
                    () -> assertEquals(LocalDate.of(2025, 3, 17), rule.calculateNextDate(START, 2)),
                    () -> assertEquals(LocalDate.of(2025, 3, 19), rule.calculateNextDate(START, 3))
            );
        }

        @Test
        @DisplayName("Начальная дата между выбранными днями сдвигается к ближайшему из них")
        void startBetweenSelectedDays() {
            RepeatRule rule = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.THURSDAY});
            LocalDate tuesday = LocalDate.of(2025, 3, 11);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 3, 13), rule.calculateNextDate(tuesday, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 17), rule.calculateNextDate(tuesday, 1)),
                    () -> assertEquals(LocalDate.of(2025, 3, 20), rule.calculateNextDate(tuesday, 2))
            );
        }

        @Test
        @DisplayName("Порядок дней в массиве не важен")
        void orderDoesNotMatter() {
            RepeatRule sorted = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.FRIDAY});
            RepeatRule reversed = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.FRIDAY, DayOfWeek.MONDAY});

            for (int i = 0; i < 20; i++) {
                assertEquals(sorted.calculateNextDate(START, i), reversed.calculateNextDate(START, i),
                        "repeat #" + i);
            }
        }

        @Test
        @DisplayName("Повторяющиеся дни в массиве учитываются один раз")
        void duplicatesAreIgnored() {
            RepeatRule unique = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY});
            RepeatRule withDuplicates = new WeekdayRepeat(
                    new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY});

            for (int i = 0; i < 20; i++) {
                assertEquals(unique.calculateNextDate(START, i), withDuplicates.calculateNextDate(START, i),
                        "repeat #" + i);
            }
        }

        @Test
        @DisplayName("Массив из одного дня ведёт себя как конструктор с одним днём")
        void singleElementArrayMatchesSingleDay() {
            RepeatRule fromArray = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.THURSDAY});
            RepeatRule fromDay = new WeekdayRepeat(DayOfWeek.THURSDAY);

            for (int i = 0; i < 20; i++) {
                assertEquals(fromDay.calculateNextDate(START, i), fromArray.calculateNextDate(START, i),
                        "repeat #" + i);
            }
        }

        @Test
        @DisplayName("Все семь дней недели дают повтор каждый день")
        void allSevenDaysRepeatDaily() {
            RepeatRule rule = new WeekdayRepeat(DayOfWeek.values());

            for (int i = 0; i < 30; i++) {
                assertEquals(START.plusDays(i), rule.calculateNextDate(START, i), "repeat #" + i);
            }
        }

        @Test
        @DisplayName("Даты строго возрастают и всегда попадают на выбранные дни")
        void datesIncreaseAndMatchSelectedDays() {
            RepeatRule rule = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.TUESDAY, DayOfWeek.SATURDAY});

            LocalDate previous = null;
            for (int i = 0; i < 50; i++) {
                LocalDate current = rule.calculateNextDate(START, i);

                DayOfWeek day = current.getDayOfWeek();
                assertTrue(day == DayOfWeek.TUESDAY || day == DayOfWeek.SATURDAY, "repeat #" + i + " -> " + day);
                if (previous != null) {
                    assertTrue(current.isAfter(previous), "repeat #" + i + ": " + previous + " -> " + current);
                }
                previous = current;
            }
        }

        @Test
        @DisplayName("Большой repeat: 300 вхождений по пн/ср/пт — ровно 100 недель")
        void largeRepeatCountsWholeWeeks() {
            RepeatRule rule = new WeekdayRepeat(
                    new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY});

            assertAll(
                    () -> assertEquals(START.plusWeeks(100), rule.calculateNextDate(START, 300)),
                    () -> assertEquals(START.plusWeeks(100).plusDays(2), rule.calculateNextDate(START, 301))
            );
        }

        @Test
        @DisplayName("Переход через границу недели: пятница -> понедельник")
        void crossesWeekBoundary() {
            RepeatRule rule = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.FRIDAY});
            LocalDate friday = LocalDate.of(2025, 3, 14);

            assertAll(
                    () -> assertEquals(friday, rule.calculateNextDate(friday, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 17), rule.calculateNextDate(friday, 1))
            );
        }

        @Test
        @DisplayName("Переход через конец года")
        void crossesYearEnd() {
            RepeatRule rule = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.MONDAY, DayOfWeek.FRIDAY});
            LocalDate tuesday = LocalDate.of(2025, 12, 30);

            assertAll(
                    () -> assertEquals(LocalDate.of(2026, 1, 2), rule.calculateNextDate(tuesday, 0)),
                    () -> assertEquals(LocalDate.of(2026, 1, 5), rule.calculateNextDate(tuesday, 1))
            );
        }

        @Test
        @DisplayName("Попадает на 29 февраля в високосном году")
        void landsOnLeapDay() {
            RepeatRule rule = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.TUESDAY, DayOfWeek.THURSDAY});
            LocalDate monday = LocalDate.of(2024, 2, 26);

            assertAll(
                    () -> assertEquals(LocalDate.of(2024, 2, 27), rule.calculateNextDate(monday, 0)),
                    () -> assertEquals(LocalDate.of(2024, 2, 29), rule.calculateNextDate(monday, 1)),
                    () -> assertEquals(LocalDate.of(2024, 3, 5), rule.calculateNextDate(monday, 2))
            );
        }

        @Test
        @DisplayName("В невисокосном году после 27 февраля идёт 4 марта")
        void skipsLeapDayInCommonYear() {
            RepeatRule rule = new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.TUESDAY, DayOfWeek.THURSDAY});
            LocalDate monday = LocalDate.of(2025, 2, 24);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 2, 25), rule.calculateNextDate(monday, 0)),
                    () -> assertEquals(LocalDate.of(2025, 2, 27), rule.calculateNextDate(monday, 1)),
                    () -> assertEquals(LocalDate.of(2025, 3, 4), rule.calculateNextDate(monday, 2))
            );
        }

        @Test
        @DisplayName("Пустой массив дней отклоняется")
        void emptyArrayRejected() {
            assertThrows(InvalidRepeatRuleException.class, () -> new WeekdayRepeat(new DayOfWeek[0]));
        }

        @Test
        @DisplayName("Массив null и null внутри массива отклоняются")
        void nullArrayOrElementRejected() {
            assertAll(
                    () -> assertThrows(InvalidRepeatRuleException.class, () -> new WeekdayRepeat((DayOfWeek[]) null)),
                    () -> assertThrows(InvalidRepeatRuleException.class,
                            () -> new WeekdayRepeat(new DayOfWeek[] {DayOfWeek.MONDAY, null}))
            );
        }

        @Test
        @DisplayName("Изменение переданного массива после создания не влияет на правило")
        void arrayIsDefensivelyCopied() {
            DayOfWeek[] days = {DayOfWeek.MONDAY};
            RepeatRule rule = new WeekdayRepeat(days);

            days[0] = DayOfWeek.FRIDAY;

            assertEquals(START, rule.calculateNextDate(START, 0));
        }
    }
}
