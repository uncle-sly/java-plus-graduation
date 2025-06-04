package ru.yandex.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {

    // веса, которые пользователи назначили событиям
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>();
    // сумма всех весов от пользователей для каждого события. для вычисления знаменателя в формуле косинусной похожести
    private final Map<Long, Double> eventWeightSums = new HashMap<>();
    // суммы минимальных весов между парами событий(по пользователям). числитель формулы похожести
    private final Map<Long, Map<Long, Double>> minWeightSums = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> processUserActions(SpecificRecordBase record) {
        UserActionAvro action = (UserActionAvro) record;
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Double newWeight = getWeight(action.getActionType());
        // старый вес, если был
        Double oldWeight = userEventWeights.getOrDefault(eventId, new HashMap<>()).getOrDefault(userId, 0.0);

        log.info("Обработка действия пользователя: userId={}, eventId={}, type={}, старый вес={}, новый вес={}",
                userId, eventId, action.getActionType(), oldWeight, newWeight);

        if (newWeight <= oldWeight) {
            log.debug("Вес не изменился: старый={}, новый={}, — перерасчёт не требуется", oldWeight, newWeight);
            return List.of();
        }

        // обновление весов и + разницу к общей сумме весов события
        double weightDiff = newWeight - oldWeight;
        updateUserWeight(eventId, userId, newWeight);
        updateEventWeightSum(eventId, weightDiff);

        log.debug("Вес пользователя обновлён. Разница весов: {}", weightDiff);

        // ищу другие события, с которыми этот пользователь взаимодействовал
        List<Long> similarEventIds = getSimilarEvents(userId, eventId);
        if (similarEventIds.isEmpty()) {
            log.debug("Похожих событий для пользователя {} не найдено", userId);
            return List.of();
        }

        log.debug("Найдены похожие события для пользователя {}: {}", userId, similarEventIds);

        // пересчет минимальных весов для пар eventId <-> otherEventId по данному пользователю
        updateMinWeightSums(eventId, userId, oldWeight, newWeight, similarEventIds);

        // обрабатываю пары событий в отсортированном порядке (a < b), чтобы избежать дубликатов
        List<EventSimilarityAvro> result = new ArrayList<>();
        for (Long otherEventId : similarEventIds) {
            long a = Math.min(eventId, otherEventId);
            long b = Math.max(eventId, otherEventId);
            // расчет похожести по формуле:
            // similarity = сумма min(весов) / (√суммы весов события A × √суммы весов события B)
            double numerator = minWeightSums.getOrDefault(a, Map.of()).getOrDefault(b, 0.0);
            double denominator = Math.sqrt(eventWeightSums.getOrDefault(a, 1.0)) * Math.sqrt(eventWeightSums.getOrDefault(b, 1.0));
            double similarity = numerator / denominator;

            log.debug("Вычислена похожесть: событиеA={}, событиеB={}, значение={}", a, b, similarity);

            result.add(EventSimilarityAvro.newBuilder()
                    .setEventA(a)
                    .setEventB(b)
                    .setScore(similarity)
                    .setTimestamp(action.getTimestamp())
                    .build());
        }

        log.info("Результат похожести для действия пользователя userId={} по eventId={}: {}", userId, eventId, result);
        return result;
    }

    // обновляю (или добавляю) вес пользователя для конкретного события
    private void updateUserWeight(Long eventId, Long userId, Double newWeight) {
        userEventWeights.computeIfAbsent(eventId, k -> new HashMap<>()).put(userId, newWeight);
        log.debug("Обновлён вес пользователя {} для события {}: {}", userId, eventId, newWeight);
    }

    // добавляю разницу весов в сумму весов события
    private void updateEventWeightSum(Long eventId, Double diff) {
        eventWeightSums.merge(eventId, diff, Double::sum);
        log.debug("Сумма весов события {} обновлена на +{} (итого: {})", eventId, diff, eventWeightSums.get(eventId));
    }

    // поиск событий, с которыми этот же пользователь уже взаимодействовал (кроме текущего). Они потенциально схожи
    private List<Long> getSimilarEvents(Long userId, Long currentEventId) {
        List<Long> eventIds = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userEventWeights.entrySet()) {
            Long otherEventId = entry.getKey();
            if (!otherEventId.equals(currentEventId)) {
                Double weight = entry.getValue().getOrDefault(userId, 0.0);
                if (weight > 0) {
                    eventIds.add(otherEventId);
                }
            }
        }
        return eventIds;
    }

    // пересчет суммы минимальных весов между парами событий, которые пользователь "оценил" (через действия)
    // эти значения используются в числителе формулы похожести
    private void updateMinWeightSums(Long eventId, Long userId, Double oldWeight, Double newWeight, List<Long> otherEventIds) {
        for (Long otherEventId : otherEventIds) {
            Double otherWeight = userEventWeights.getOrDefault(otherEventId, Map.of()).getOrDefault(userId, 0.0);
            long a = Math.min(eventId, otherEventId);
            long b = Math.max(eventId, otherEventId);
            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            double delta = newMin - oldMin;

            minWeightSums
                    .computeIfAbsent(a, k -> new HashMap<>())
                    .merge(b, delta, Double::sum);

            log.debug("Обновлён min вес для пары событий ({}, {}): изменение на {}", a, b, delta);
        }
    }

    private Double getWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

}