package ru.yandex.practicum.aggregator;

import java.util.*;
public class UserEventWeightStorage {

    private final Map<Long, Map<Long, Double>> data = new HashMap<>();

    public Double getWeight(Long eventId, Long userId) {
        return data.getOrDefault(eventId, Map.of()).getOrDefault(userId, 0.0);
    }
    public void setWeight(Long eventId, Long userId, Double weight) {
        data.computeIfAbsent(eventId, k -> new HashMap<>()).put(userId, weight);
    }

    public Map<Long, Double> getUsersForEvent(Long eventId) {
        return data.getOrDefault(eventId, Map.of());
    }
    public Set<Long> getAllEventIds() {
        return data.keySet();
    }

    public Double getWeightForOtherEvent(Long otherEventId, Long userId) {
        return data.getOrDefault(otherEventId, Map.of()).getOrDefault(userId, 0.0);
    }

}