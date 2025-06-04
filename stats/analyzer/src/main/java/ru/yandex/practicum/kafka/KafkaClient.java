package ru.yandex.practicum.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface KafkaClient extends AutoCloseable {

    Consumer<Long, EventSimilarityAvro> getEventSimilarityConsumer();

    Consumer<Long, UserActionAvro> getUserActionConsumer();

}
