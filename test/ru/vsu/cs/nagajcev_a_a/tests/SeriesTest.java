package ru.vsu.cs.nagajcev_a_a.tests;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.Timeout;
import ru.vsu.cs.nagajcev_a_a.event.AfterCount;
import ru.vsu.cs.nagajcev_a_a.event.Never;
import ru.vsu.cs.nagajcev_a_a.event.RepeatingEvent;
import ru.vsu.cs.nagajcev_a_a.event.EndRule;
import ru.vsu.cs.nagajcev_a_a.event.Until;
import ru.vsu.cs.nagajcev_a_a.event.DateConflictException;
import ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeat;
import ru.vsu.cs.nagajcev_a_a.repeat.CustomMonthRepeatPattern;
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
        @DisplayName("Отмена переноса у вхождения, которое не переносили, ничего не делает")
        void removingAbsentMoveIsNoOp() {
            RepeatingEvent event = weeklyFromMarch3();
            event.removeException(2);

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
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
    @DisplayName("Ограничение серии")
    class Limits {

        private RepeatingEvent weeklyFromMarch3(EndRule end) {
            return new RepeatingEvent("Планёрка", "Еженедельная", MONDAY_MARCH_3, new NDayRepeat(7), end);
        }

        @Test
        @DisplayName("Never — серия без конца, как у конструктора без ограничения")
        void neverIsUnbounded() {
            LocalDate farFrom = LocalDate.of(2030, 3, 1);
            LocalDate farTo = LocalDate.of(2030, 3, 31);

            assertEquals(series(SeriesTest.weeklyFromMarch3(), farFrom, farTo),
                    series(weeklyFromMarch3(new Never()), farFrom, farTo));
        }

        @Test
        @DisplayName("AfterCount(4) — ровно 4 вхождения")
        void afterCountLimitsOccurrences() {
            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24"),
                    series(weeklyFromMarch3(new AfterCount(4)), MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("AfterCount(1) — только первое вхождение")
        void afterCountOne() {
            assertEquals(dates("2025-03-03"),
                    series(weeklyFromMarch3(new AfterCount(1)), MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("После конца серии по количеству — пусто")
        void afterCountIntervalAfterEndIsEmpty() {
            assertTrue(weeklyFromMarch3(new AfterCount(4))
                    .getSeries(LocalDate.of(2025, 3, 25), LocalDate.of(2030, 1, 1)).isEmpty());
        }

        @Test
        @DisplayName("Количество считается по номерам: последнее вхождение можно перенести за исходный конец")
        void lastOccurrenceMovedPastOriginalEndStays() {
            RepeatingEvent event = weeklyFromMarch3(new AfterCount(4));
            event.addException(3, LocalDate.of(2025, 4, 2));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-04-02"),
                    series(event, MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Перенос не добавляет лишнего вхождения в конец")
        void moveDoesNotExtendSeries() {
            RepeatingEvent event = weeklyFromMarch3(new AfterCount(4));
            event.addException(1, march(28));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24", "2025-03-28"),
                    series(event, MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Дата несуществующего вхождения за концом серии не считается занятой")
        void dateBeyondCountIsNotOccupied() {
            RepeatingEvent event = weeklyFromMarch3(new AfterCount(4));
            event.addException(1, march(31));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Удалённое вхождение расходует счётчик: серия не продлевается")
        void deletedOccurrenceConsumesCount() {
            RepeatingEvent event = weeklyFromMarch3(new AfterCount(4));
            event.deleteOccurrence(1);

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24"),
                    series(event, MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Until — конечная дата на вхождении включается")
        void untilIsInclusive() {
            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24"),
                    series(weeklyFromMarch3(new Until(march(24))), MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Until — конечная дата между вхождениями")
        void untilBetweenOccurrences() {
            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17"),
                    series(weeklyFromMarch3(new Until(march(23))), MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Until на дате начала — одно вхождение")
        void untilOnStartDate() {
            assertEquals(dates("2025-03-03"),
                    series(weeklyFromMarch3(new Until(MONDAY_MARCH_3)), MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Until проверяется по исходной дате: последнее вхождение можно перенести за конечную дату")
        void untilChecksOriginalDate() {
            RepeatingEvent event = weeklyFromMarch3(new Until(march(24)));
            event.addException(3, march(26));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-26"),
                    series(event, MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Дата несуществующего вхождения после Until не считается занятой")
        void dateBeyondUntilIsNotOccupied() {
            RepeatingEvent event = weeklyFromMarch3(new Until(march(24)));
            event.addException(1, march(31));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, LocalDate.of(2025, 5, 1)));
        }

        @Test
        @DisplayName("Перенос несуществующего вхождения — IllegalArgumentException")
        void movingNonexistentOccurrenceThrows() {
            RepeatingEvent byCount = weeklyFromMarch3(new AfterCount(4));
            RepeatingEvent byDate = weeklyFromMarch3(new Until(march(24)));

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> byCount.addException(4, march(5))),
                    () -> assertThrows(IllegalArgumentException.class, () -> byDate.addException(4, march(5)))
            );
        }

        @Test
        @DisplayName("Ограниченная серия совпадает с первыми вхождениями бесконечной (все правила)")
        void limitedSeriesMatchesOracle() {
            LocalDate start = LocalDate.of(2024, 1, 31);
            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2025, 12, 31);

            for (var entry : sampleRules().entrySet()) {
                RepeatRule rule = entry.getValue();
                List<LocalDate> unlimited = oracle(rule, start, Map.of(), from, to);
                for (int count = 1; count <= 12; count++) {
                    RepeatingEvent byCount = new RepeatingEvent("Событие", "", start, rule, new AfterCount(count));

                    assertEquals(unlimited.subList(0, count), series(byCount, from, to),
                            entry.getKey() + ": AfterCount(" + count + ")");
                }
                for (int days = 0; days < 200; days += 13) {
                    LocalDate endDate = start.plusDays(days);
                    RepeatingEvent byDate = new RepeatingEvent("Событие", "", start, rule, new Until(endDate));

                    assertEquals(oracle(rule, start, Map.of(), from, endDate), series(byDate, from, to),
                            entry.getKey() + ": Until(" + endDate + ")");
                }
            }
        }

        @Test
        @DisplayName("setEndRule обрезает серию, в том числе перенесённые вхождения за новым концом")
        void setEndRuleHidesMovedOccurrencesBeyondNewEnd() {
            RepeatingEvent event = SeriesTest.weeklyFromMarch3();
            event.addException(4, march(28));
            event.setEndRule(new AfterCount(3));

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @Timeout(value = 1, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
        @DisplayName("Огромная правая граница интервала не ломает ограниченную серию")
        void hugeIntervalEndWithLimitedSeries() {
            RepeatingEvent event = weeklyFromMarch3(new AfterCount(2));

            assertEquals(dates("2025-03-03", "2025-03-10"), series(event, MARCH_1, LocalDate.MAX));
        }

        @Test
        @DisplayName("AfterCount(0) и отрицательное количество — IllegalArgumentException")
        void nonPositiveCountThrows() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> new AfterCount(0)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new AfterCount(-1))
            );
        }

        @Test
        @DisplayName("Until(null) и ограничение null в конструкторе — NullPointerException")
        void nullLimitsThrow() {
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> new Until(null)),
                    () -> assertThrows(NullPointerException.class, () -> weeklyFromMarch3(null))
            );
        }
    }

    @Nested
    @Order(5)
    @DisplayName("Удаление одного вхождения")
    class DeleteOccurrence {

        @Test
        @DisplayName("Удаление убирает вхождение и не трогает остальные")
        void deleteRemovesOnlyThatOccurrence() {
            RepeatingEvent event = weeklyFromMarch3();
            event.deleteOccurrence(2);

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Удаление первого вхождения")
        void deleteFirstOccurrence() {
            RepeatingEvent event = weeklyFromMarch3();
            event.deleteOccurrence(0);

            assertEquals(dates("2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Восстановление удалённого вхождения")
        void undeleteRestoresOccurrence() {
            RepeatingEvent event = weeklyFromMarch3();
            event.deleteOccurrence(2);
            event.undeleteOccurrence(2);

            assertEquals(dates("2025-03-03", "2025-03-10", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Дата удалённого вхождения свободна для переноса")
        void deletedDateIsFreeForMove() {
            RepeatingEvent event = weeklyFromMarch3();
            event.deleteOccurrence(2);
            event.addException(1, march(17));

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Удаление перенесённого вхождения убирает его с новой даты")
        void deleteMovedOccurrence() {
            RepeatingEvent event = weeklyFromMarch3();
            event.addException(1, march(20));
            event.deleteOccurrence(1);

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Удаление перенесённого вхождения в ограниченной серии убирает его с новой даты")
        void deleteMovedOccurrenceInLimitedSeries() {
            RepeatingEvent event = new RepeatingEvent("Планёрка", "", MONDAY_MARCH_3, new NDayRepeat(7),
                    new AfterCount(4));
            event.addException(1, march(20));
            event.deleteOccurrence(1);

            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Перенос удалённого вхождения — IllegalArgumentException для любого ограничения")
        void movingDeletedOccurrenceThrows() {
            List<EndRule> endRules = List.of(new Never(), new AfterCount(4), new Until(march(24)));

            for (EndRule endRule : endRules) {
                RepeatingEvent event = new RepeatingEvent("Планёрка", "", MONDAY_MARCH_3, new NDayRepeat(7), endRule);
                event.deleteOccurrence(1);

                assertThrows(IllegalArgumentException.class, () -> event.addException(1, march(20)),
                        endRule.toString());
                assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24"),
                        series(event, MARCH_1, march(24)), endRule.toString());
            }
        }

        @Test
        @DisplayName("Восстановление, когда исходная дата уже занята, — DateConflictException и вхождение остаётся удалённым")
        void undeleteOntoOccupiedDateThrows() {
            RepeatingEvent event = weeklyFromMarch3();
            event.deleteOccurrence(2);
            event.addException(1, march(17));

            assertThrows(DateConflictException.class, () -> event.undeleteOccurrence(2));
            assertEquals(dates("2025-03-03", "2025-03-17", "2025-03-24", "2025-03-31"),
                    series(event, MARCH_1, APRIL_1));
        }

        @Test
        @DisplayName("Удаление с отрицательным номером — IllegalArgumentException")
        void negativeDeleteThrows() {
            RepeatingEvent event = weeklyFromMarch3();

            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> event.deleteOccurrence(-1)),
                    () -> assertThrows(IllegalArgumentException.class, () -> event.undeleteOccurrence(-1))
            );
        }
    }

    @Nested
    @Order(6)
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
