package ru.vsu.cs.nagajcev_a_a.event;

import java.time.LocalDate;

public class RepeatingEvent extends Event {
    public RepeatingEvent(String name, String description, LocalDate date) {
        super(name, description, date);
    }
}
