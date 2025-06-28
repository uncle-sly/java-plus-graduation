package ru.yandex.practicum.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.model.EventSimilarity;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@UtilityClass
@Slf4j
public class EventSimilarityMapper {

    public EventSimilarity mapToEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        return EventSimilarity.builder()
                .eventA(eventSimilarityAvro.getEventA())
                .eventB(eventSimilarityAvro.getEventB())
                .score(eventSimilarityAvro.getScore())
                .timestamp(mapToTimestamp(eventSimilarityAvro.getTimestamp()))
                .build();
    }

    private LocalDateTime mapToTimestamp(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

}
