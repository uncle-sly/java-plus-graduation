package ru.yandex.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public interface KafkaClient extends AutoCloseable {

    Producer<Long, SpecificRecordBase> getProducer();

    Consumer<Long, SpecificRecordBase> getConsumer();

    ProducerRecord<Long, SpecificRecordBase> getProducerRecord(String topic, SpecificRecordBase record);

}