package com.dochiri.habitservice.application.common;

public sealed interface Patchable<T> {

    @SuppressWarnings("unchecked")
    static <T> Patchable<T> undefined() {
        return (Patchable<T>) Undefined.INSTANCE;
    }

    static <T> Patchable<T> of(T value) {
        return new Present<>(value);
    }

    default boolean isPresent() {
        return this instanceof Present<T>;
    }

    default T orElse(T fallback) {
        return this instanceof Present<T> present ? present.value() : fallback;
    }

    record Present<T>(T value) implements Patchable<T> {
    }

    final class Undefined<T> implements Patchable<T> {
        private static final Undefined<?> INSTANCE = new Undefined<>();

        private Undefined() {
        }
    }

}
