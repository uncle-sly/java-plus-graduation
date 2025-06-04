package ru.yandex.practicum.model;

public enum UserActionType {

    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    private final double weight;

    UserActionType(double weight) {
        this.weight = weight;
    }

    public double getScore() {
        return weight;
    }

}
