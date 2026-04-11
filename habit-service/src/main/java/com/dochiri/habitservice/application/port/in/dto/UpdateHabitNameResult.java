package com.dochiri.habitservice.application.port.in.dto;

public record UpdateHabitNameResult(
        String id,
        String name
) {
}