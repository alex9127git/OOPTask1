package ru.vsu.cs.nagajcev_a_a.event;

import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;
import java.time.LocalDate;
import java.util.*;

public final class RepeatingEvent extends Event {
    private RepeatRule repeatRule;
    private EndRule endRule;
    private final Map<Integer, LocalDate> exceptions = new HashMap<>();
    private final Set<Integer> deleted = new TreeSet<>();

    public RepeatingEvent(String name, String description, LocalDate date, RepeatRule repeatRule) {
        this(name, description, date, repeatRule, new Never());
    }

    public RepeatingEvent(String name, String description, LocalDate date, RepeatRule repeatRule, EndRule endRule) {
        super(name, description, date);
        Objects.requireNonNull(repeatRule, "Правило повтора не может быть равно null");
        Objects.requireNonNull(endRule, "Правило конца не может быть равно null");
        this.repeatRule = repeatRule;
        this.endRule = endRule;
    }

    public void addException(int repeatId, LocalDate newDate) {
        if (repeatId < 0) throw new IllegalArgumentException("ID повторения не может быть отрицательным");
        Objects.requireNonNull(newDate, "Дата не может быть равна null");
        if (!exists(repeatId)) {
            throw new IllegalArgumentException("Данное повторение не существует или было удалено");
        }
        if (!exceptions.containsKey(repeatId) &&
                this.repeatRule.calculateNextDate(this.getDate(), repeatId).equals(newDate)) return;
        if (exceptions.containsKey(repeatId) && exceptions.get(repeatId).equals(newDate)) return;
        if (!this.getSeries(newDate, newDate).isEmpty())
            throw new DateConflictException("На эту дату уже есть повтор этого события");
        exceptions.put(repeatId, newDate);
    }

    public void removeException(int repeatId) {
        if (repeatId < 0) throw new IllegalArgumentException("ID повторения не может быть отрицательным");
        LocalDate baseDate = this.repeatRule.calculateNextDate(this.getDate(), repeatId);
        if (exceptions.containsKey(repeatId) && !this.getSeries(baseDate, baseDate).isEmpty())
            throw new DateConflictException("На эту дату уже есть повтор этого события");
        exceptions.remove(repeatId);
    }

    public void deleteOccurrence(int repeatId) {
        if (repeatId < 0) throw new IllegalArgumentException("ID повторения не может быть отрицательным");
        deleted.add(repeatId);
        exceptions.remove(repeatId);
    }

    public void undeleteOccurrence(int repeatId) {
        if (repeatId < 0) throw new IllegalArgumentException("ID повторения не может быть отрицательным");
        LocalDate baseDate = this.repeatRule.calculateNextDate(this.getDate(), repeatId);
        if (!this.getSeries(baseDate, baseDate).isEmpty())
            throw new DateConflictException("На эту дату уже есть повтор этого события");
        deleted.remove(repeatId);
    }

    public void setRepeatRule(RepeatRule repeatRule) {
        Objects.requireNonNull(repeatRule, "Правило повтора не может быть равно null");
        this.repeatRule = repeatRule;
        this.exceptions.clear();
    }

    public void setEndRule(EndRule endRule) {
        Objects.requireNonNull(endRule, "Правило конца не может быть равно null");
        this.endRule = endRule;
    }

    public Set<LocalDate> getSeries(LocalDate from, LocalDate to) {
        if (from == null || to == null) throw new NullPointerException("Даты не могут быть равны null");
        Set<LocalDate> result = new TreeSet<>();
        LocalDate date = LocalDate.MIN;
        int r = -1;
        while (date.isBefore(from)) {
            r++;
            date = repeatRule.calculateNextDate(this.getDate(), r);
        }
        while (!date.isAfter(to)) {
            if (!exceptions.containsKey(r) && !deleted.contains(r)) {
                if (this.endRule instanceof AfterCount) {
                    if (r < ((AfterCount) this.endRule).count()) result.add(date);
                    else break;
                } else if (this.endRule instanceof Until) {
                    if (!date.isAfter(((Until) this.endRule).endDate())) result.add(date);
                    else break;
                } else {
                    result.add(date);
                }
            }
            r++;
            date = repeatRule.calculateNextDate(this.getDate(), r);
        }
        for (Map.Entry<Integer, LocalDate> e : exceptions.entrySet()) {
            date = e.getValue();
            if (!date.isBefore(from) && !date.isAfter(to) && exists(e.getKey())) {
                result.add(date);
            }
        }
        return result;
    }

    public boolean exists(int repeatId) {
        if (deleted.contains(repeatId)) return false;
        if (this.endRule instanceof AfterCount) {
            return repeatId < ((AfterCount) this.endRule).count();
        } else if (this.endRule instanceof Until) {
            LocalDate date = this.repeatRule.calculateNextDate(this.getDate(), repeatId);
            return !date.isAfter(((Until) this.endRule).endDate());
        } else {
            return true;
        }
    }
}
