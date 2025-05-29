package ru.yandex.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public interface KafkaProducer extends AutoCloseable {
    Producer<String, SpecificRecordBase> getProducer();

    ProducerRecord<String, SpecificRecordBase> getProducerRecord(String topic, SpecificRecordBase record);
}
