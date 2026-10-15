package ru.vsu.cs.nagajcev_a_a.event;

import java.time.LocalDate;

public class SingleEvent extends Event {
    public SingleEvent(String name, String description, LocalDate date) {
        super(name, description, date);
    }
}
