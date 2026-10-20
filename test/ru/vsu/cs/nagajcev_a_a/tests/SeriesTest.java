package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import ru.vsu.cs.nagajcev_a_a.event.RepeatingEvent;
import ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern;
import ru.vsu.cs.nagajcev_a_a.repeat.DateConflictException;
import ru.vsu.cs.nagajcev_a_a.repeat.NDayRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.NMonthRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.NYearRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;
import ru.vsu.cs.nagajcev_a_a.repeat.WeekdayRepeat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Интервал getSeries(from, to) включает обе границы.
 */
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class SeriesTest {

    private static final LocalDate MONDAY_MARCH_3 = LocalDate.of(2025, 3, 3);
    private static final LocalDate MARCH_1 = LocalDate.of(2025, 3, 1);
    private static final LocalDate APRIL_1 = LocalDate.of(2025, 4, 1);

    private static RepeatingEvent weeklyFromMarch3() {
        return new RepeatingEvent("Планёрка", "Еженедельная", MONDAY_MARCH_3, new NDayRepeat(7));
    }

    private static LocalDate march(int day) {
        return LocalDate.of(2025, 3, day);
    }

    private static List<LocalDate> dates(String... isoDates) {
        List<LocalDate> result = new ArrayList<>();
        for (String iso : isoDates) {
            result.add(LocalDate.parse(iso));
        }
        return result;
    }

    private static List<LocalDate> series(RepeatingEvent event, LocalDate from, LocalDate to) {
        return new ArrayList<>(event.getSeries(from, to));
    }

    @Nested
    @Order(1)
    @DisplayName("Развёртка серии на интервал")
    class Expansion {

        @Test
        @DisplayName("Все вхождения внутри интервала по порядку")
        void listsOccurrencesInOrder() {
            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(weeklyFromMarch3(), MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Вхождение ровно в from входит в результат")
        void fromIsInclusive() {
            assertEquals(dates("2025-03-10", "2025-03-17"),
                    series(weeklyFromMarch3(), march(10), march(20)));
        }

        @Test
        @DisplayName("Вхождение ровно в to входит в результат")
        void toIsInclusive() {
            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17"),
                    series(weeklyFromMarch3(), MARCH_1, march(17)));
        }

        @Test
        @DisplayName("from и to — соседние вхождения: оба в результате")
        void bothBoundsOnOccurrences() {
            assertEquals(dates("2025-03-10", "2025-03-17"),
                    series(weeklyFromMarch3(), march(10), march(17)));
        }

        @Test
        @DisplayName("to совпадает с началом серии — одно вхождение")
        void toEqualsSeriesStart() {
            assertEquals(dates("2025-03-03"),
                    series(weeklyFromMarch3(), LocalDate.of(2025, 1, 1), MONDAY_MARCH_3));
        }

        @Test
        @DisplayName("Интервал целиком до начала серии — пусто")
        void intervalBeforeSeriesStartIsEmpty() {
            assertTrue(weeklyFromMarch3().getSeries(LocalDate.of(2025, 1, 1), march(2)).isEmpty());
        }

        @Test
        @DisplayName("Интервал строго между двумя вхождениями — пусто")
        void intervalBetweenOccurrencesIsEmpty() {
            assertTrue(weeklyFromMarch3().getSeries(march(4), march(9)).isEmpty());
        }

        @Test
        @DisplayName("from == to на дате вхождения — одно вхождение")
        void singleDayIntervalOnOccurrence() {
            assertEquals(dates("2025-03-17"), series(weeklyFromMarch3(), march(17), march(17)));
        }

        @Test
        @DisplayName("from == to не на дате вхождения — пусто")
        void singleDayIntervalOffOccurrence() {
            assertTrue(weeklyFromMarch3().getSeries(march(18), march(18)).isEmpty());
        }

        @Test
        @DisplayName("Интервал далеко после начала серии")
        void intervalFarAfterStart() {
            assertEquals(dates("2026-03-02", "2026-03-09"),
                    series(weeklyFromMarch3(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15)));
        }

        @Test
        @DisplayName("Дата начала не подходит под правило — серия начинается с ближайшего подходящего дня")
        void startDateNotMatchingRule() {
            RepeatingEvent event = new RepeatingEvent("Спорт", "", LocalDate.of(2025, 3, 4),
                    new WeekdayRepeat(new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY}));

            assertEquals(dates("2025-03-05", "2025-03-10", "2025-03-12"),
                    series(event, MARCH_1, march(12)));
        }

        @Test
        @DisplayName("31-е число: в коротких месяцах последний день, потом снова 31-е")
        void thirtyFirstIsClampedWithoutDrift() {
            RepeatingEvent event = new RepeatingEvent("Отчёт", "", LocalDate.of(2025, 1, 31), new NMonthRepeat(31));

            assertEquals(dates("2025-01-31", "2025-02-28", "2025-03-31", "2025-04-30", "2025-05-31"),
                    series(event, LocalDate.of(2025, 1, 31), LocalDate.of(2025, 5, 31)));
        }

        @Test
        @DisplayName("29 февраля: в невисокосные годы 28-е, в високосный снова 29-е")
        void leapDayAcrossYears() {
            RepeatingEvent event = new RepeatingEvent("Юбилей", "", LocalDate.of(2024, 2, 29), new NYearRepeat(1));

            assertEquals(dates("2024-02-29", "2025-02-28", "2026-02-28", "2027-02-28", "2028-02-29"),
                    series(event, LocalDate.of(2024, 2, 29), LocalDate.of(2028, 2, 29)));
        }

        @Test
        @DisplayName("Изменение возвращённого набора не влияет на серию")
        void returnedSetIsNotInternalState() {
            RepeatingEvent event = weeklyFromMarch3();
            Set<LocalDate> first = event.getSeries(MARCH_1, APRIL_1);
            first.clear();

            assertEquals(5, event.getSeries(MARCH_1, APRIL_1).size());
        }

        @Test
        @DisplayName("Смена правила меняет развёртку")
        void setRepeatRuleChangesExpansion() {
            RepeatingEvent event = weeklyFromMarch3();
            event.setRepeatRule(new NDayRepeat(14));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-31"), series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Развёртка совпадает с независимым расчётом для разных правил и интервалов")
        void expansionMatchesOracle() {
            LocalDate start = LocalDate.of(2024, 1, 31);
            for (var entry : sampleRules().entrySet()) {
                RepeatRule rule = entry.getValue();
                RepeatingEvent event = new RepeatingEvent("Событие", "", start, rule);
                for (int fromOffset = 0; fromOffset < 120; fromOffset += 7) {
                    for (int length = 0; length < 60; length += 5) {
                        LocalDate from = start.plusDays(fromOffset);
                        LocalDate to = from.plusDays(length);

                        assertEquals(oracle(rule, start, Map.of(), from, to), series(event, from, to),
                                entry.getKey() + ": [" + from + ", " + to + "]");
                    }
                }
            }
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Перенос одного вхождения")
    class MoveOccurrence {

        @Test
        @DisplayName("Перенос вперёд не трогает остальные вхождения")
        void moveForward() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(2, march(18));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-18", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Перенос назад не трогает остальные вхождения")
        void moveBackward() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(2, march(15));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-15", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Перенос через соседнее вхождение: еженедельное событие сдвинуто на 10 дней")
        void moveOverNeighbourOccurrence() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-20", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Перенос первого вхождения (номер 0)")
        void moveFirstOccurrence() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(0, march(4));

            assertEquals(dates("2025-03-04", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Перенос за конец интервала не обрывает следующие вхождения")
        void moveBeyondIntervalEndKeepsLaterOccurrences() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, LocalDate.of(2025, 4, 9));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Перенос до начала интервала убирает вхождение из результата")
        void moveBeforeIntervalStartExcludesOccurrence() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(2, march(7));

            assertEquals(dates("2025-03-10", "2025-03-24", "2025-03-31"),
                    series(event, march(8), APRIL_1));
        }

        @Test
        @DisplayName("Вхождение на to перенесено за интервал — пропадает")
        void occurrenceOnToMovedOut() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(4, LocalDate.of(2025, 4, 5));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24"),
                    series(event, MARCH_1, march(31)));
        }

        @Test
        @DisplayName("Перенос из-за конца интервала внутрь него")
        void moveIntoIntervalFromOutside() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(5, march(28));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-28", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Перенос из-за конца интервала ровно на from")
        void moveFromOutsideExactlyOntoFrom() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(6, march(25));

            assertEquals(dates("2025-03-25", "2025-03-31"),
                    series(event, march(25), APRIL_1));
        }

        @Test
        @DisplayName("Перенос из-за конца интервала ровно на to")
        void moveFromOutsideExactlyOntoTo() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(6, march(28));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-28"),
                    series(event, MARCH_1, march(28)));
        }

        @Test
        @DisplayName("Отмена переноса возвращает исходную дату")
        void removeExceptionRestoresOriginalDate() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(2, march(18));
            event.removeException(2);

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Повторный перенос того же вхождения заменяет прежний")
        void secondMoveReplacesFirst() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(2, march(18));
            event.addException(2, march(20));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-20", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Смена правила сбрасывает переносы")
        void setRepeatRuleClearsMoves() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));
            event.setRepeatRule(new NDayRepeat(14));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-31"), series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Каждый одиночный перенос совпадает с независимым расчётом (все правила, сдвиги ±40 дней)")
        void everySingleMoveMatchesOracle() {
            LocalDate start = LocalDate.of(2024, 1, 31);
            LocalDate from = LocalDate.of(2024, 3, 1);
            LocalDate to = LocalDate.of(2024, 5, 31);

            for (var entry : sampleRules().entrySet()) {
                RepeatRule rule = entry.getValue();
                int occurrencesToCheck = occurrencesUpTo(rule, start, to.plusDays(40));
                for (int occurrence = 0; occurrence < occurrencesToCheck; occurrence++) {
                    int moved = occurrence;
                    for (int shift = -40; shift <= 40; shift += 9) {
                        LocalDate target = rule.calculateNextDate(start, moved).plusDays(shift);
                        RepeatingEvent event = new RepeatingEvent("Событие", "", start, rule);
                        String context = entry.getKey() + ": вхождение #" + moved + " перенесено на " + target;

                        if (isOccupiedByAnotherOccurrence(rule, start, moved, target)) {
                            assertThrows(DateConflictException.class, () -> event.addException(moved, target), context);
                            assertEquals(oracle(rule, start, Map.of(), from, to), series(event, from, to), context);
                        } else {
                            event.addException(moved, target);
                            assertEquals(oracle(rule, start, Map.of(moved, target), from, to), series(event, from, to),
                                    context);
                        }
                    }
                }
            }
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Перенос на занятую дату")
    class MoveConflict {

        @Test
        @DisplayName("Перенос на дату обычного вхождения — DateConflictException")
        void moveOntoRegularOccurrenceThrows() {
            RepeatingEvent event = weeklyFromMarch3();

            assertEquals("На эту дату уже есть повтор этого события",
                    assertThrows(DateConflictException.class, () -> event.addException(1, march(17))).getMessage());
        }

        @Test
        @DisplayName("Отклонённый перенос не меняет серию")
        void rejectedMoveLeavesSeriesUnchanged() {
            RepeatingEvent event = weeklyFromMarch3();

            assertThrows(DateConflictException.class, () -> event.addException(1, march(17)));
            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Отклонённый повторный перенос сохраняет прежний перенос")
        void rejectedSecondMoveKeepsFirstMove() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));

            assertThrows(DateConflictException.class, () -> event.addException(1, march(24)));
            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-20", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Перенос на дату, куда уже перенесено другое вхождение, — DateConflictException")
        void moveOntoAnotherMovedOccurrenceThrows() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));

            assertThrows(DateConflictException.class, () -> event.addException(3, march(20)));
        }

        @Test
        @DisplayName("Перенос на дату, освобождённую другим переносом, разрешён")
        void moveOntoFreedDateIsAllowed() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));
            event.addException(2, march(10));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-20", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Конфликт с вхождением далеко в будущем тоже обнаруживается")
        void conflictFarInFutureIsDetected() {
            RepeatingEvent event = weeklyFromMarch3();

            assertThrows(DateConflictException.class, () -> event.addException(0, LocalDate.of(2030, 3, 4)));
        }

        @Test
        @DisplayName("Перенос на дату до начала серии разрешён")
        void moveBeforeSeriesStartIsAllowed() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(0, LocalDate.of(2025, 2, 28));

            assertEquals(dates("2025-02-28", "2025-03-10"),
                    series(event, LocalDate.of(2025, 2, 1), march(10)));
        }

        @Test
        @DisplayName("Перенос вхождения на его же исходную дату — не конфликт")
        void moveOntoOwnOriginalDateIsAllowed() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(10));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Возврат перенесённого вхождения на исходную дату — не конфликт")
        void moveBackOntoOwnOriginalDateIsAllowed() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));
            event.addException(1, march(10));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Повторный перенос на ту же дату — не конфликт")
        void repeatingSameMoveIsAllowed() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));
            event.addException(1, march(20));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-20", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Отмена переноса, когда исходная дата уже занята, — DateConflictException и перенос остаётся")
        void removingMoveOntoOccupiedOriginalThrows() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));
            event.addException(2, march(10));

            assertThrows(DateConflictException.class, () -> event.removeException(1));
            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-20", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }
    }

    @Nested
    @Order(4)
    @DisplayName("Границы и исключения")
    class Errors {

        @Test
        @DisplayName("null в границах интервала — NullPointerException")
        void nullBoundsThrow() {
            RepeatingEvent event = weeklyFromMarch3();

            assertAll(
                    () -> assertEquals("Даты не могут быть равны null",
                            assertThrows(NullPointerException.class, () -> event.getSeries(null, APRIL_1)).getMessage()),
                    () -> assertEquals("Даты не могут быть равны null",
                            assertThrows(NullPointerException.class, () -> event.getSeries(MARCH_1, null)).getMessage())
            );
        }

        @Test
        @DisplayName("null вместо правила в конструкторе — NullPointerException сразу")
        void nullRuleInConstructorThrows() {
            assertThrows(NullPointerException.class,
                    () -> new RepeatingEvent("Планёрка", "", MONDAY_MARCH_3, null));
        }

        @Test
        @DisplayName("null вместо даты начала в конструкторе — NullPointerException сразу")
        void nullStartDateInConstructorThrows() {
            assertThrows(NullPointerException.class,
                    () -> new RepeatingEvent("Планёрка", "", null, new NDayRepeat(7)));
        }

        @Test
        @DisplayName("setRepeatRule(null) — NullPointerException")
        void nullRuleInSetterThrows() {
            RepeatingEvent event = weeklyFromMarch3();

            assertThrows(NullPointerException.class, () -> event.setRepeatRule(null));
        }

        @Test
        @DisplayName("Перенос вхождения с отрицательным номером — IllegalArgumentException при добавлении")
        void negativeOccurrenceNumberThrowsOnAdd() {
            RepeatingEvent event = weeklyFromMarch3();

            assertThrows(IllegalArgumentException.class, () -> event.addException(-1, march(5)));
        }

        @Test
        @DisplayName("Перенос на дату null — NullPointerException при добавлении")
        void nullTargetDateThrowsOnAdd() {
            RepeatingEvent event = weeklyFromMarch3();

            assertThrows(NullPointerException.class, () -> event.addException(1, null));
        }
    }

    private static Map<String, RepeatRule> sampleRules() {
        return Map.of(
                "каждые 5 дней", new NDayRepeat(5),
                "пн и чт", new WeekdayRepeat(new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.THURSDAY}),
                "31-е число", new NMonthRepeat(31),
                "последняя пятница", new CustomMonthRepeat(CustomMonthRepeatPattern.LAST, DayOfWeek.FRIDAY)
        );
    }

    private static int occurrencesUpTo(RepeatRule rule, LocalDate start, LocalDate limit) {
        int count = 0;
        while (!rule.calculateNextDate(start, count).isAfter(limit)) {
            count++;
        }
        return count;
    }

    private static boolean isOccupiedByAnotherOccurrence(RepeatRule rule, LocalDate start, int moved, LocalDate target) {
        for (int i = 0; !rule.calculateNextDate(start, i).isAfter(target); i++) {
            if (i != moved && rule.calculateNextDate(start, i).equals(target)) return true;
        }
        return false;
    }

    private static List<LocalDate> oracle(RepeatRule rule, LocalDate start, Map<Integer, LocalDate> moves,
                                          LocalDate from, LocalDate to) {
        int maxShiftDays = 60;
        Set<LocalDate> expected = new TreeSet<>();
        int occurrences = occurrencesUpTo(rule, start, to.plusDays(maxShiftDays));
        for (int i = 0; i < occurrences; i++) {
            LocalDate date = moves.getOrDefault(i, rule.calculateNextDate(start, i));
            if (!date.isBefore(from) && !date.isAfter(to)) expected.add(date);
        }
        return new ArrayList<>(expected);
    }
}
