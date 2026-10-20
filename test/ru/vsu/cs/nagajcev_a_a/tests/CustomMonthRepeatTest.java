package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern;
import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern.FIRST;
import static ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern.FOURTH;
import static ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern.FOURTH_TO_LAST;
import static ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern.LAST;
import static ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern.SECOND;
import static ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern.SECOND_TO_LAST;
import static ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern.THIRD;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DisplayName("CustomMonthRepeat")
class CustomMonthRepeatTest {

    private static final LocalDate OCTOBER_FIRST = LocalDate.of(2025, 10, 1);

    private static LocalDate expectedOccurrence(YearMonth month, CustomMonthRepeatPattern pattern, DayOfWeek weekDay) {
        return month.atDay(1).with(TemporalAdjusters.dayOfWeekInMonth(pattern.getValue(), weekDay));
    }

    @Nested
    @Order(1)
    @DisplayName("Базовое поведение")
    class Basic {

        private final RepeatRule secondTuesday = new CustomMonthRepeat(SECOND, DayOfWeek.TUESDAY);

        @Test
        @DisplayName("repeat = 0 до вхождения сдвигает дату на второй вторник этого месяца")
        void zeroRepeatBeforeOccurrence() {
            assertEquals(LocalDate.of(2025, 10, 14), secondTuesday.calculateNextDate(OCTOBER_FIRST, 0));
        }

        @Test
        @DisplayName("repeat = 0 в день вхождения возвращает саму начальную дату")
        void zeroRepeatOnOccurrence() {
            LocalDate occurrence = LocalDate.of(2025, 10, 14);

            assertEquals(occurrence, secondTuesday.calculateNextDate(occurrence, 0));
        }

        @Test
        @DisplayName("repeat = 0 после вхождения переходит на второй вторник следующего месяца")
        void zeroRepeatAfterOccurrence() {
            assertEquals(LocalDate.of(2025, 11, 11), secondTuesday.calculateNextDate(LocalDate.of(2025, 10, 15), 0));
        }

        @Test
        @DisplayName("repeat = N даёт второй вторник через N месяцев")
        void nthRepeatMovesNMonths() {
            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 11, 11), secondTuesday.calculateNextDate(OCTOBER_FIRST, 1)),
                    () -> assertEquals(LocalDate.of(2025, 12, 9), secondTuesday.calculateNextDate(OCTOBER_FIRST, 2)),
                    () -> assertEquals(LocalDate.of(2026, 10, 13), secondTuesday.calculateNextDate(OCTOBER_FIRST, 12))
            );
        }

        @Test
        @DisplayName("Результат не зависит от того, на какой день недели приходится начальная дата")
        void resultDoesNotDependOnStartWeekday() {
            for (int day = 1; day <= 14; day++) {
                LocalDate start = LocalDate.of(2025, 10, day);

                assertEquals(LocalDate.of(2025, 10, 14), secondTuesday.calculateNextDate(start, 0), "start " + start);
            }
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Порядковые номера с начала и с конца месяца")
    class Ordinals {

        @Test
        @DisplayName("Первый понедельник, когда 1-е число — понедельник")
        void firstWeekdayOnFirstDayOfMonth() {
            RepeatRule rule = new CustomMonthRepeat(FIRST, DayOfWeek.MONDAY);

            assertEquals(LocalDate.of(2025, 9, 1), rule.calculateNextDate(LocalDate.of(2025, 9, 1), 0));
        }

        @Test
        @DisplayName("Третий вторник")
        void thirdTuesday() {
            RepeatRule rule = new CustomMonthRepeat(THIRD, DayOfWeek.TUESDAY);

            assertEquals(LocalDate.of(2025, 10, 21), rule.calculateNextDate(OCTOBER_FIRST, 0));
        }

        @Test
        @DisplayName("Последняя пятница, когда она же последний день месяца")
        void lastWeekdayOnLastDayOfMonth() {
            RepeatRule rule = new CustomMonthRepeat(LAST, DayOfWeek.FRIDAY);

            assertEquals(LocalDate.of(2025, 10, 31), rule.calculateNextDate(OCTOBER_FIRST, 0));
        }

        @Test
        @DisplayName("Последнее воскресенье, когда месяц кончается не воскресеньем")
        void lastWeekdayBeforeMonthEnd() {
            RepeatRule rule = new CustomMonthRepeat(LAST, DayOfWeek.SUNDAY);

            assertEquals(LocalDate.of(2025, 10, 26), rule.calculateNextDate(OCTOBER_FIRST, 0));
        }

        @Test
        @DisplayName("В месяце с пятью средами четвёртая и последняя среда различаются")
        void fourthAndLastDifferInFiveWeekdayMonth() {
            DayOfWeek wednesday = DayOfWeek.WEDNESDAY;

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 10, 22),
                            new CustomMonthRepeat(FOURTH, wednesday).calculateNextDate(OCTOBER_FIRST, 0)),
                    () -> assertEquals(LocalDate.of(2025, 10, 29),
                            new CustomMonthRepeat(LAST, wednesday).calculateNextDate(OCTOBER_FIRST, 0)),
                    () -> assertEquals(LocalDate.of(2025, 10, 22),
                            new CustomMonthRepeat(SECOND_TO_LAST, wednesday).calculateNextDate(OCTOBER_FIRST, 0)),
                    () -> assertEquals(LocalDate.of(2025, 10, 8),
                            new CustomMonthRepeat(FOURTH_TO_LAST, wednesday).calculateNextDate(OCTOBER_FIRST, 0))
            );
        }

        @Test
        @DisplayName("Начальная дата после последнего вхождения месяца переходит на следующий месяц")
        void startAfterLastOccurrenceMovesToNextMonth() {
            RepeatRule rule = new CustomMonthRepeat(LAST, DayOfWeek.MONDAY);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 10, 27), rule.calculateNextDate(LocalDate.of(2025, 10, 27), 0)),
                    () -> assertEquals(LocalDate.of(2025, 11, 24), rule.calculateNextDate(LocalDate.of(2025, 10, 28), 0))
            );
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Краевые случаи календаря")
    class CalendarEdges {

        @Test
        @DisplayName("Февраль 2026 из ровно четырёх недель: первое и четвёртое с конца воскресенье совпадают")
        void fourWeekFebruary() {
            LocalDate february = LocalDate.of(2026, 2, 1);

            assertAll(
                    () -> assertEquals(LocalDate.of(2026, 2, 1),
                            new CustomMonthRepeat(FIRST, DayOfWeek.SUNDAY).calculateNextDate(february, 0)),
                    () -> assertEquals(LocalDate.of(2026, 2, 1),
                            new CustomMonthRepeat(FOURTH_TO_LAST, DayOfWeek.SUNDAY).calculateNextDate(february, 0)),
                    () -> assertEquals(LocalDate.of(2026, 2, 22),
                            new CustomMonthRepeat(LAST, DayOfWeek.SUNDAY).calculateNextDate(february, 0)),
                    () -> assertEquals(LocalDate.of(2026, 2, 28),
                            new CustomMonthRepeat(FOURTH, DayOfWeek.SATURDAY).calculateNextDate(february, 0))
            );
        }

        @Test
        @DisplayName("Последний четверг високосного февраля — 29 февраля")
        void lastThursdayOfLeapFebruary() {
            RepeatRule rule = new CustomMonthRepeat(LAST, DayOfWeek.THURSDAY);

            assertAll(
                    () -> assertEquals(LocalDate.of(2024, 2, 29), rule.calculateNextDate(LocalDate.of(2024, 2, 1), 0)),
                    () -> assertEquals(LocalDate.of(2024, 2, 22),
                            new CustomMonthRepeat(FOURTH, DayOfWeek.THURSDAY).calculateNextDate(LocalDate.of(2024, 2, 1), 0))
            );
        }

        @Test
        @DisplayName("Последний четверг невисокосного февраля и следующего марта")
        void lastThursdayOfCommonFebruary() {
            RepeatRule rule = new CustomMonthRepeat(LAST, DayOfWeek.THURSDAY);
            LocalDate february = LocalDate.of(2025, 2, 1);

            assertAll(
                    () -> assertEquals(LocalDate.of(2025, 2, 27), rule.calculateNextDate(february, 0)),
                    () -> assertEquals(LocalDate.of(2025, 3, 27), rule.calculateNextDate(february, 1))
            );
        }

        @Test
        @DisplayName("Переход через конец года")
        void crossesYearEnd() {
            RepeatRule rule = new CustomMonthRepeat(SECOND, DayOfWeek.TUESDAY);

            assertEquals(LocalDate.of(2026, 1, 13), rule.calculateNextDate(LocalDate.of(2025, 12, 20), 0));
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Общие свойства")
    class Properties {

        @Test
        @DisplayName("Для любого шаблона, дня недели и начальной даты вхождения идут по месяцам подряд")
        void occurrencesFollowConsecutiveMonths() {
            for (CustomMonthRepeatPattern pattern : CustomMonthRepeatPattern.values()) {
                for (DayOfWeek weekDay : DayOfWeek.values()) {
                    RepeatRule rule = new CustomMonthRepeat(pattern, weekDay);

                    for (LocalDate start = LocalDate.of(2024, 1, 1); start.getYear() == 2024; start = start.plusDays(1)) {
                        String context = pattern + " " + weekDay + ", start " + start;
                        YearMonth startMonth = YearMonth.from(start);
                        boolean startMonthOccurrencePassed =
                                expectedOccurrence(startMonth, pattern, weekDay).isBefore(start);
                        YearMonth firstMonth = startMonthOccurrencePassed ? startMonth.plusMonths(1) : startMonth;

                        for (int i = 0; i < 3; i++) {
                            assertEquals(expectedOccurrence(firstMonth.plusMonths(i), pattern, weekDay),
                                    rule.calculateNextDate(start, i), context + ", repeat #" + i);
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Шаг на repeat + 1 совпадает с шагом на 1 от вхождения номер repeat")
        void stepFromOccurrenceMatchesStepFromStart() {
            for (CustomMonthRepeatPattern pattern : CustomMonthRepeatPattern.values()) {
                RepeatRule rule = new CustomMonthRepeat(pattern, DayOfWeek.FRIDAY);

                for (int i = 0; i < 24; i++) {
                    LocalDate occurrence = rule.calculateNextDate(OCTOBER_FIRST, i);

                    assertEquals(rule.calculateNextDate(OCTOBER_FIRST, i + 1), rule.calculateNextDate(occurrence, 1),
                            pattern + ", repeat #" + i);
                }
            }
        }
    }

    @Nested
    @Order(5)
    @DisplayName("Границы и исключения")
    class Boundaries {

        @Test
        @DisplayName("repeat = Integer.MAX_VALUE не переполняется")
        void maxRepeatDoesNotOverflow() {
            RepeatRule rule = new CustomMonthRepeat(SECOND, DayOfWeek.TUESDAY);
            YearMonth expectedMonth = YearMonth.of(2025, 10).plusMonths(Integer.MAX_VALUE);

            assertEquals(expectedOccurrence(expectedMonth, SECOND, DayOfWeek.TUESDAY),
                    rule.calculateNextDate(OCTOBER_FIRST, Integer.MAX_VALUE));
        }

        @Test
        @DisplayName("Выход за LocalDate.MAX выбрасывает DateTimeException")
        void pastMaxDateThrows() {
            RepeatRule rule = new CustomMonthRepeat(FIRST, DayOfWeek.MONDAY);

            assertThrows(DateTimeException.class, () -> rule.calculateNextDate(LocalDate.MAX, 0));
        }

        @Test
        @DisplayName("null вместо шаблона или дня недели отклоняется")
        void nullArgumentsRejected() {
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> new CustomMonthRepeat(null, DayOfWeek.MONDAY)),
                    () -> assertThrows(NullPointerException.class, () -> new CustomMonthRepeat(FIRST, null))
            );
        }

        @Test
        @DisplayName("Отрицательный repeat отклоняется")
        void negativeRepeatRejected() {
            RepeatRule rule = new CustomMonthRepeat(SECOND, DayOfWeek.TUESDAY);

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> rule.calculateNextDate(OCTOBER_FIRST, -1)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> rule.calculateNextDate(OCTOBER_FIRST, Integer.MIN_VALUE))
            );
        }

        @Test
        @DisplayName("Дата null отклоняется с NullPointerException и понятным сообщением")
        void nullDateRejected() {
            RepeatRule rule = new CustomMonthRepeat(SECOND, DayOfWeek.TUESDAY);

            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> rule.calculateNextDate(null, 0));
            assertEquals("Дата не может быть равна null", exception.getMessage());
        }
    }
}
