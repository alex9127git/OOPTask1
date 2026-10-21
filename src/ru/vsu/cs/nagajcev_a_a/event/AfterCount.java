package ru.vsu.cs.nagajcev_a_a.event;

public record AfterCount(int count) implements EndRule {
    public AfterCount {
        if (count <= 0)
            throw new IllegalArgumentException("Количество дней в серии не может быть меньше или равно нулю");
    }
}
