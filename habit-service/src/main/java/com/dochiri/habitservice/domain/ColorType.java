package com.dochiri.habitservice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ColorType {

    BLUE("#3b82f6"),
    GREEN("#10b981"),
    RED("#ef4444"),
    YELLOW("#f59e0b"),
    PURPLE("#8b5cf6"),
    PINK("#ec4899");

    private final String hexValue;

}