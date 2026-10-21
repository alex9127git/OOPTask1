package ru.vsu.cs.nagajcev_a_a.event;

import java.time.LocalDate;

/**
 * Одиночное событие, которое происходит один раз.
 */
public final class SingleEvent extends Event {
    /**
     * Создаёт одиночное событие.
     *
     * @param name        название события, может быть {@code null}
     * @param description описание события, может быть {@code null}
     * @param date        дата события
     * @throws NullPointerException если {@code date} равна {@code null}
     */
    public SingleEvent(String name, String description, LocalDate date) {
        super(name, description, date);
    }
}
