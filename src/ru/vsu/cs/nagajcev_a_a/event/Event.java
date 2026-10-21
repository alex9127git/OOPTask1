package ru.vsu.cs.nagajcev_a_a.event;

import java.time.LocalDate;
import java.util.Objects;

public abstract sealed class Event permits SingleEvent, RepeatingEvent {
    private static long nextId = 1;

    private EventId id;
    private String name;
    private String description;
    private LocalDate date;

    public Event(String name, String description, LocalDate date) {
        Objects.requireNonNull(date, "Дата не может быть равна null");
        id = new EventId(nextId++);
        this.name = name;
        this.description = description;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public EventId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return (Objects.equals(id, event.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
