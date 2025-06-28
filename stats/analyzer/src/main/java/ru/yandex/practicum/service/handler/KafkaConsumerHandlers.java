package ru.yandex.practicum.service.handler;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface KafkaConsumerHandlers {

    void eventsSimilarityGot(EventSimilarityAvro eventSimilarityAvro);

    void userActionGot(UserActionAvro userActionAvro);

}