package ru.vsu.cs.nagajcev_a_a.endRule;

/**
 * Условие окончания серии повторяющегося события.
 * <p>
 * Вхождение серии существует, только если оно не выходит за это условие.
 * Возможны три варианта: {@link Never}, {@link AfterCount} и {@link Until}.
 */
public sealed interface EndRule permits Never, AfterCount, Until {}

