package ru.vsu.cs.nagajcev_a_a.event;

import ru.vsu.cs.nagajcev_a_a.repeat.RepeatRule;
import java.time.LocalDate;
import java.util.*;

/**
 * Повторяющееся событие — серия вхождений, построенная по правилу повторения от даты начала.
 * <p>
 * Вхождения нумеруются с нуля по порядку исходных дат. Номер остаётся за вхождением и после переноса,
 * поэтому по нему можно перенести, удалить или восстановить конкретное вхождение,
 * не затрагивая остальные. Два вхождения серии никогда не приходятся на одну дату.
 */
public final class RepeatingEvent extends Event {
    private RepeatRule repeatRule;
    private EndRule endRule;
    private final Map<Integer, LocalDate> exceptions = new HashMap<>();
    private final Set<Integer> deleted = new TreeSet<>();

    /**
     * Создаёт бесконечную серию.
     *
     * @param name        название события, может быть {@code null}
     * @param description описание события, может быть {@code null}
     * @param date        дата начала серии
     * @param repeatRule  правило повторения
     * @throws NullPointerException если {@code date} или {@code repeatRule} равны {@code null}
     */
    public RepeatingEvent(String name, String description, LocalDate date, RepeatRule repeatRule) {
        this(name, description, date, repeatRule, new Never());
    }

    /**
     * Создаёт серию с условием окончания.
     *
     * @param name        название события, может быть {@code null}
     * @param description описание события, может быть {@code null}
     * @param date        дата начала серии
     * @param repeatRule  правило повторения
     * @param endRule     условие окончания серии
     * @throws NullPointerException если {@code date}, {@code repeatRule} или {@code endRule} равны {@code null}
     */
    public RepeatingEvent(String name, String description, LocalDate date, RepeatRule repeatRule, EndRule endRule) {
        super(name, description, date);
        Objects.requireNonNull(repeatRule, "Правило повтора не может быть равно null");
        Objects.requireNonNull(endRule, "Правило конца не может быть равно null");
        this.repeatRule = repeatRule;
        this.endRule = endRule;
    }

    /**
     * Переносит одно вхождение на другую дату, не меняя остальные.
     * Повторный перенос того же вхождения заменяет прежний. Перенос на дату, где это вхождение
     * уже стоит, ничего не меняет.
     *
     * @param repeatId номер вхождения, начиная с 0
     * @param newDate  новая дата вхождения
     * @throws IllegalArgumentException если {@code repeatId} отрицательный, а также если вхождение не существует или удалено
     * @throws NullPointerException     если {@code newDate} равна {@code null}
     * @throws DateConflictException    если на {@code newDate} уже стоит другое вхождение серии
     */
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

    /**
     * Отменяет перенос вхождения, возвращая его на исходную дату.
     * Если вхождение не переносилось, ничего не происходит.
     *
     * @param repeatId номер вхождения, начиная с 0
     * @throws IllegalArgumentException если {@code repeatId} отрицательный
     * @throws DateConflictException    если исходную дату уже заняло другое вхождение
     */
    public void removeException(int repeatId) {
        if (repeatId < 0) throw new IllegalArgumentException("ID повторения не может быть отрицательным");

        LocalDate baseDate = this.repeatRule.calculateNextDate(this.getDate(), repeatId);
        if (exceptions.containsKey(repeatId) && !this.getSeries(baseDate, baseDate).isEmpty())
            throw new DateConflictException("На эту дату уже есть повтор этого события");
        exceptions.remove(repeatId);
    }

    /**
     * Удаляет одно вхождение из серии, не меняя остальные. Перенос этого вхождения, если он был,
     * тоже отменяется. Дата удалённого вхождения освобождается для переносов других.
     *
     * @param repeatId номер вхождения, начиная с 0
     * @throws IllegalArgumentException если {@code repeatId} отрицательный
     */
    public void deleteOccurrence(int repeatId) {
        if (repeatId < 0) throw new IllegalArgumentException("ID повторения не может быть отрицательным");

        deleted.add(repeatId);
        exceptions.remove(repeatId);
    }

    /**
     * Восстанавливает удалённое вхождение на его исходной дате.
     *
     * @param repeatId номер вхождения, начиная с 0
     * @throws IllegalArgumentException если {@code repeatId} отрицательный
     * @throws DateConflictException    если исходную дату уже заняло другое вхождение
     */
    public void undeleteOccurrence(int repeatId) {
        if (repeatId < 0) throw new IllegalArgumentException("ID повторения не может быть отрицательным");
        if (!deleted.contains(repeatId)) return;

        LocalDate baseDate = this.repeatRule.calculateNextDate(this.getDate(), repeatId);
        if (!this.getSeries(baseDate, baseDate).isEmpty())
            throw new DateConflictException("На эту дату уже есть повтор этого события");
        deleted.remove(repeatId);
    }

    /**
     * Меняет правило повторения. Все переносы и удаления при этом сбрасываются:
     * при новом правиле вхождения с теми же номерами приходятся на другие даты.
     *
     * @param repeatRule новое правило повторения
     * @throws NullPointerException если {@code repeatRule} равно {@code null}
     */
    public void setRepeatRule(RepeatRule repeatRule) {
        Objects.requireNonNull(repeatRule, "Правило повтора не может быть равно null");
        this.repeatRule = repeatRule;
        this.exceptions.clear();
        this.deleted.clear();
    }

    /**
     * Меняет условие окончания серии. Вхождения за новым концом, в том числе перенесённые,
     * в серию больше не попадают.
     *
     * @param endRule новое условие окончания
     * @throws NullPointerException если {@code endRule} равно {@code null}
     */
    public void setEndRule(EndRule endRule) {
        Objects.requireNonNull(endRule, "Правило конца не может быть равно null");
        this.endRule = endRule;
    }

    /**
     * Разворачивает серию в даты на интервале {@code [from, to]}, обе границы включительно.
     * Учитываются условие окончания, переносы и удаления: перенесённое вхождение попадает
     * в результат по новой дате, удалённое не попадает.
     *
     * @param from начало интервала
     * @param to   конец интервала
     * @return новое отсортированное множество дат; изменение его не влияет на серию
     * @throws NullPointerException если {@code from} или {@code to} равны {@code null}
     */
    public Set<LocalDate> getSeries(LocalDate from, LocalDate to) {
        if (from == null || to == null) throw new NullPointerException("Даты не могут быть равны null");
        Set<LocalDate> result = new TreeSet<>();
        LocalDate date = LocalDate.MIN;
        int r = -1;
        while (date.isBefore(from)) {
            r++;
            date = repeatRule.calculateNextDate(this.getDate(), r);
        }
        boolean seriesFinished = false;
        while (!date.isAfter(to) && !seriesFinished) {
            if (!exceptions.containsKey(r) && !deleted.contains(r)) {
                if (!isInSeries(r)) break;
                result.add(date);
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

    /**
     * Проверяет, существует ли вхождение: не вышло ли оно за условие окончания и не удалено ли.
     *
     * @param repeatId номер вхождения, начиная с 0
     * @return {@code true}, если вхождение есть в серии
     */
    public boolean exists(int repeatId) {
        if (deleted.contains(repeatId)) return false;
        return isInSeries(repeatId);
    }

    private boolean isInSeries(int repeatId) {
        LocalDate date = this.repeatRule.calculateNextDate(this.getDate(), repeatId);
        return switch (endRule) {
            case AfterCount rule -> repeatId < rule.count();
            case Until rule -> !date.isAfter(rule.endDate());
            case Never ignored -> true;
        };
    }

    /**
     * Находит ближайшее вхождение серии в указанную дату или позже.
     * Учитываются условие окончания, переносы и удаления: перенесённое вхождение ищется
     * по новой дате, удалённое пропускается.
     * <p>
     * Чтобы пройти серию по порядку, следующий вызов делается со дня после найденной даты.
     *
     * @param date дата, начиная с которой ищется вхождение, включительно
     * @return дата ближайшего вхождения или пустой {@link Optional}, если серия к этой дате закончилась
     * @throws NullPointerException если {@code date} равна {@code null}
     */
    public Optional<LocalDate> nextFrom(LocalDate date) {
        Objects.requireNonNull(date, "Дата не может быть равна null");
        if (date.equals(LocalDate.MAX)) return Optional.empty();

        LocalDate next = null;
        for (int r = 0; isInSeries(r); r++) {
            LocalDate original = this.repeatRule.calculateNextDate(this.getDate(), r);
            if (!original.isBefore(date) && !exceptions.containsKey(r) && !deleted.contains(r)) {
                next = original;
                break;
            }
        }

        for (Map.Entry<Integer, LocalDate> move : exceptions.entrySet()) {
            LocalDate moved = move.getValue();
            if (exists(move.getKey()) && !moved.isBefore(date) && (next == null || moved.isBefore(next))) {
                next = moved;
            }
        }
        return Optional.ofNullable(next);
    }
}
