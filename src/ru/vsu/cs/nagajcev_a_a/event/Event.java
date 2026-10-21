package ru.vsu.cs.nagajcev_a_a.event;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Событие календаря: одиночное ({@link SingleEvent}) или повторяющееся ({@link RepeatingEvent}).
 * <p>
 * Событие — сущность: каждому событию при создании выдаётся уникальный {@link EventId},
 * и только по нему определяется равенство. Название, описание и дату можно менять,
 * и событие при этом остаётся тем же, в том числе в {@code HashSet} и {@code HashMap}.
 */
public abstract sealed class Event permits SingleEvent, RepeatingEvent {
    private static long nextId = 1;

    private final EventId id;
    private String name;
    private String description;
    private LocalDate date;

    /**
     * Создаёт событие и выдаёт ему новый идентификатор.
     *
     * @param name        название события, может быть {@code null}
     * @param description описание события, может быть {@code null}
     * @param date        дата события; для серии — дата её начала
     * @throws NullPointerException если {@code date} равна {@code null}
     */
    public Event(String name, String description, LocalDate date) {
        Objects.requireNonNull(date, "Дата не может быть равна null");
        id = new EventId(nextId++);
        this.name = name;
        this.description = description;
        this.date = date;
    }

    /**
     * Возвращает название события.
     *
     * @return название, может быть {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Меняет название события. На равенство событий не влияет.
     *
     * @param name новое название, может быть {@code null}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает описание события.
     *
     * @return описание, может быть {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Меняет описание события. На равенство событий не влияет.
     *
     * @param description новое описание, может быть {@code null}
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает дату события; для серии — дату её начала.
     *
     * @return дата события
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Меняет дату события; для серии — дату её начала. На равенство событий не влияет.
     *
     * @param date новая дата
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Возвращает идентификатор события.
     *
     * @return идентификатор, одинаковый на всё время жизни события
     */
    public EventId getId() {
        return id;
    }

    /**
     * Сравнивает события по идентификатору. Два события с одинаковыми полями,
     * созданные отдельно, не равны.
     *
     * @param o объект для сравнения
     * @return {@code true}, если {@code o} — событие с тем же идентификатором
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return (Objects.equals(id, event.id));
    }

    /**
     * Возвращает хеш-код идентификатора, поэтому он не меняется при изменении события.
     *
     * @return хеш-код события
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
