package ru.vsu.cs.nagajcev_a_a.event;

import java.time.LocalDate;

public record Until(LocalDate endDate) implements EndRule {
    public Until {
        if (endDate == null) throw new NullPointerException("Дата не может быть равна null");
    }
}
