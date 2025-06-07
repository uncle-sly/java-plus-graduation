package ru.yandex.practicum.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserActionType {

    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    private final double weight;

}