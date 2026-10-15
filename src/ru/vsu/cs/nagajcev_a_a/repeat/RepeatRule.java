package ru.vsu.cs.nagajcev_a_a.repeat;

import java.time.LocalDate;

public interface RepeatRule {
    public LocalDate calculateNextDate(LocalDate date, int repeat);
}
