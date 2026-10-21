package ru.vsu.cs.nagajcev_a_a;

import ru.vsu.cs.nagajcev_a_a.endRule.AfterCount;
import ru.vsu.cs.nagajcev_a_a.endRule.Never;
import ru.vsu.cs.nagajcev_a_a.endRule.Until;
import ru.vsu.cs.nagajcev_a_a.event.*;
import ru.vsu.cs.nagajcev_a_a.repeat.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Консольная демонстрация модели повторяющихся событий: по очереди показывает каждую операцию базового уровня.
 */
public class Main {
    public static final RepeatingEvent diffurLecture = new RepeatingEvent(
                "Диффуры", "Каверина В.К. 479", LocalDate.of(2026, 9, 18), new NDayRepeat(7));

    private Main() {
    }

    /**
     * Запускает демонстрацию.
     *
     * @param args не используются
     */
    public static void main(String[] args) {
        List<Event> calendar = createCalendar();

        showCalendar(calendar);
        showExceptions();
        showExpansion(calendar);
        showNextOccurrence();
        showCalendarEdgeCases();
        showIdentity();
    }

    private static List<Event> createCalendar() {
        List<Event> calendar = new ArrayList<>();
        calendar.add(new RepeatingEvent(
                "Диффуры", "Атанов А.В. 305П", LocalDate.of(2026, 9, 14), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "Матан", "Атанов А.В. 292", LocalDate.of(2026, 9, 21), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "Матан", "Михин Е.А. 477", LocalDate.of(2026, 9, 14), new NDayRepeat(7)));
        calendar.add(new RepeatingEvent(
                "Физра", "я не знаю препода по физре", LocalDate.of(2026, 9, 14),
                new WeekdayRepeat(new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY}),
                new Until(LocalDate.of(2026, 12, 15))));
        calendar.add(new RepeatingEvent(
                "ООП", "Самойлов Н.К. 316П", LocalDate.of(2026, 9, 23), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "Культурология", "Якушкина Е.И. 477", LocalDate.of(2026, 9, 23), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "Психология", "Горбанева Е.В. 307П", LocalDate.of(2026, 9, 23), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "Питон", "Чекмарев А.И. ДО", LocalDate.of(2026, 9, 24), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "ООП", "Самойлов Н.К. ДО", LocalDate.of(2026, 9, 17), new NDayRepeat(7)));
        calendar.add(new RepeatingEvent(
                "Культурология", "Якушкина Е.И. ДО", LocalDate.of(2026, 9, 17), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "БД", "Толстобров А.П. ДО", LocalDate.of(2026, 9, 17), new NDayRepeat(7)));
        calendar.add(new RepeatingEvent(
                "Ин яз", "Стрельникова М.А. 308", LocalDate.of(2026, 9, 25), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "Тервер", "Сынкова А.Е. 479", LocalDate.of(2026, 9, 18), new NDayRepeat(7)));
        diffurLecture.addException(3, LocalDate.of(2026, 10, 6));
        diffurLecture.deleteOccurrence(2);
        calendar.add(diffurLecture);
        calendar.add(new RepeatingEvent(
                "Психология", "Кожевникова Е.В. 479", LocalDate.of(2026, 9, 18), new NDayRepeat(7)));
        calendar.add(new RepeatingEvent(
                "КомпГГ", "Косенко И.М. 477", LocalDate.of(2026, 9, 26), new NDayRepeat(7)));
        calendar.add(new RepeatingEvent(
                "КомпГГ", "Косенко И.М. 293", LocalDate.of(2026, 9, 19), new NDayRepeat(14)));
        calendar.add(new RepeatingEvent(
                "БД", "Фролова В.В. 295", LocalDate.of(2026, 9, 26), new NDayRepeat(7)));
        calendar.add(new RepeatingEvent(
                "Тервер", "Попов М.И. 505П", LocalDate.of(2026, 9, 26), new NDayRepeat(7)));

        calendar.add(new SingleEvent(
                "Атта", "Матанализ", LocalDate.of(2026, 10, 19)));
        calendar.add(new SingleEvent(
                "Атта", "Тервер", LocalDate.of(2026, 10, 10)));
        calendar.add(new SingleEvent(
                "Атта", "Диффуры", LocalDate.of(2026, 10, 12)));
        calendar.add(new SingleEvent(
                "Атта", "Дискеты", LocalDate.of(2026, 10, 6)));
        calendar.add(new SingleEvent(
                "Атта", "Психология", LocalDate.of(2026, 10, 14)));

        calendar.add(new RepeatingEvent(
                "Сходка вайбкодеров", "не забыть принести Claude Code", LocalDate.of(2026, 9, 1),
                new CustomMonthRepeat(CustomMonthRepeatPattern.LAST, DayOfWeek.SATURDAY),
                new AfterCount(3)));

        calendar.add(new RepeatingEvent(
                "День рождения", "урааа! :D", LocalDate.of(2026, 1, 9), new NYearRepeat(1)));
        return calendar;
    }

    private static void showCalendar(List<Event> calendar) {
        printSection("Календарь: " + calendar.size() + " событий");
    }

    private static void showExceptions() {
        printSection("Недопустимые правила повторения");
        try {
            RepeatRule ignored = new WeekdayRepeat(new DayOfWeek[] {});
        } catch (InvalidRepeatRuleException e) {
            System.out.println("Нельзя повторять событие по нулю дней недели");
        }
        try {
            RepeatRule ignored = new NDayRepeat(-1);
        } catch (InvalidRepeatRuleException e) {
            System.out.println("или каждый -1 день");
        }
        try {
            RepeatRule ignored = new NYearRepeat(-1);
        } catch (InvalidRepeatRuleException e) {
            System.out.println("каждый -1 год тоже нельзя");
        }
        try {
            diffurLecture.addException(10, LocalDate.of(2026, 11, 6));
        } catch (DateConflictException e) {
            System.out.println("на существующую дату переносить событие нельзя");
        }
        diffurLecture.deleteOccurrence(7);
        try {
            diffurLecture.addException(10, LocalDate.of(2026, 11, 6));
        } catch (DateConflictException e) {
            System.out.println("это не должно напечататься, потому что мы удалили событие");
        }
        try {
            diffurLecture.undeleteOccurrence(7);
        } catch (DateConflictException e) {
            System.out.println("а это должно - мы не хотим, чтобы было две лекции в один день");
        }
    }

    private static void showExpansion(List<Event> calendar) {
        printSection("Развёртка серий на интервал");
        LocalDate from = LocalDate.of(2026, 9, 14);
        LocalDate to = from.plusWeeks(18).plusDays(-1);
        Map<LocalDate, List<String>> dates = new TreeMap<>();
        for (Event event : calendar) {
            switch (event) {
                case SingleEvent se:
                    dates.putIfAbsent(se.getDate(), new ArrayList<>());
                    dates.get(se.getDate()).add(se.toString());
                    break;
                case RepeatingEvent re:
                    for (LocalDate date : re.getSeries(from, to)) {
                        dates.putIfAbsent(date, new ArrayList<>());
                        dates.get(date).add(re.toString());
                    }
            }
        }
        for (Map.Entry<LocalDate, List<String>> entry : dates.entrySet()) {
            System.out.println(entry.getKey());
            for (String v : entry.getValue()) {
                System.out.println(v);
            }
            System.out.println();
        }
    }

    private static void showNextOccurrence() {
        printSection("Ближайшее вхождение начиная с даты");
        LocalDate date = LocalDate.of(2026, 9, 20);
        for (; date.isBefore(LocalDate.of(2026, 11, 1)); date = date.plusDays(3)) {
            System.out.println(date + ": " + diffurLecture.nextFrom(date) + " " + diffurLecture);
        }
    }

    private static void showCalendarEdgeCases() {
        printSection("Краевые случаи календаря");
        LocalDate startDate = LocalDate.of(2026, 9, 10);
        RepeatingEvent re = new RepeatingEvent(
                "Ритуал продления подписок", "чтобы было чем вайбкодить", startDate, new NMonthRepeat(31));
        for (LocalDate date : re.getSeries(startDate, startDate.plusYears(4))) {
            System.out.println(date + " " + re);
        }
    }

    private static void showIdentity() {
        printSection("Идентичность событий: equals/hashCode по EventId");
        RepeatingEvent re = new RepeatingEvent(
                "Сходка вайбкодеров", "не забыть принести Claude Code", LocalDate.of(2026, 9, 1),
                new CustomMonthRepeat(CustomMonthRepeatPattern.LAST, DayOfWeek.SATURDAY),
                new AfterCount(3));
        System.out.println(re.getId());
        re.setEndRule(new Never());
        System.out.println("ID не изменился: " + re.getId());
        System.out.println("Это прикольно, у нас ничего в хэшмапе странного не произойдёт, если мы поменяем че то");
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
