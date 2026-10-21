package ru.vsu.cs.nagajcev_a_a.event;

public record EventId(long value) {
    public EventId {
        if (value <= 0) throw new IllegalArgumentException("...");
    }
}
