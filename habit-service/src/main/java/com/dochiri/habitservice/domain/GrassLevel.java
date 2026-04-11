package com.dochiri.habitservice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GrassLevel {

    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    FULL(4);

    private final int level;

}