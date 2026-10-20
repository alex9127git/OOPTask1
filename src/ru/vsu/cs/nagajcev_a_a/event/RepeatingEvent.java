package ru.vsu.cs.nagajcev_a_a.event;

import ru.vsu.cs.nagajcev_a_a.repeat.DateConflictException;
import ru.vsu.cs.nagajcev_a_a.repeat.InvalidRepeatRuleException;
import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;

import java.time.LocalDate;
import java.util.*;

public class RepeatingEvent extends Event {
    private RepeatRule repeatRule;
    private final Map<Integer, LocalDate> exceptions = new HashMap<>();

    public RepeatingEvent(String name, String description, LocalDate date, RepeatRule repeatRule) {
        super(name, description, date);
        Objects.requireNonNull(repeatRule, "Правило повтора не может быть равно null");
        this.repeatRule = repeatRule;
    }

    public void addException(int repeatId, LocalDate newDate) {
        if (repeatId < 0) throw new IllegalArgumentException("ID повторения не может быть отрицательным");
        Objects.requireNonNull(newDate, "Дата не может быть равно null");
        if (!exceptions.containsKey(repeatId) &&
                this.repeatRule.calculateNextDate(this.date, repeatId).equals(newDate)) return;
        if (exceptions.containsKey(repeatId) && exceptions.get(repeatId).equals(newDate)) return;
        if (!this.getSeries(newDate, newDate).isEmpty())
            throw new DateConflictException("На эту дату уже есть повтор этого события");
        exceptions.put(repeatId, newDate);
    }

    public void removeException(int repeatId) {
        LocalDate baseDate = this.repeatRule.calculateNextDate(this.date, repeatId);
        if (!this.getSeries(baseDate, baseDate).isEmpty())
            throw new DateConflictException("На эту дату уже есть повтор этого события");
        exceptions.remove(repeatId);
    }

    public void setRepeatRule(RepeatRule repeatRule) {
        Objects.requireNonNull(repeatRule, "Правило повтора не может быть равно null");
        this.repeatRule = repeatRule;
        this.exceptions.clear();
    }

    public Set<LocalDate> getSeries(LocalDate from, LocalDate to) {
        if (from == null || to == null) throw new NullPointerException("Даты не могут быть равны null");
        Set<LocalDate> result = new TreeSet<>();
        LocalDate date = LocalDate.MIN;
        int r = -1;
        while (date.isBefore(from)) {
            r++;
            date = repeatRule.calculateNextDate(this.date, r);
        }
        while (!date.isAfter(to)) {
            if (!exceptions.containsKey(r)) result.add(date);
            r++;
            date = repeatRule.calculateNextDate(this.date, r);
        }
        for (Map.Entry<Integer, LocalDate> e : exceptions.entrySet()) {
            date = e.getValue();
            if (!date.isBefore(from) && !date.isAfter(to)) {
                result.add(date);
            }
        }
        return result;
    }
}
