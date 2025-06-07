package ru.yandex.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.kafka.KafkaClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final AggregatorService aggregatorService;
    private final KafkaClient kafkaClient;

    @Value("${kafka.consumer.properties.topic}")
    private String consumerTopic;
    @Value("${kafka.consumer.properties.user-consume-attempt-timeout}")
    private Duration pollTimeout;
    @Value("${kafka.producer.properties.topic}")
    private String producerTopic;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий
     */
    public void start() {
        Producer<Long, SpecificRecordBase> producer = kafkaClient.getProducer();
        Consumer<Long, SpecificRecordBase> consumer = kafkaClient.getConsumer();
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(consumerTopic));
            log.info("Подписка на UserAction топик: {} ", consumerTopic);

            while (true) {
                ConsumerRecords<Long, SpecificRecordBase> records = consumer.poll(pollTimeout);
                if (!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<Long, SpecificRecordBase> record : records) {
                        log.info("Получено: topic = {}, partition = {}, offset = {}, record = {}",
                                record.topic(), record.partition(), record.offset(), record.value());
                        List<EventSimilarityAvro> eventsSimilarity = aggregatorService.processUserActions(record.value());
                        eventsSimilarity.forEach(similarity -> {
                            log.info("Отправляем результат: {}", similarity);
                            producer.send(kafkaClient.getProducerRecord(producerTopic, similarity), (metadata, ex) -> {
                                if (ex != null) {
                                    log.error("Ошибка при отправке сообщения: {}", similarity, ex);
                                }
                            });
                        });
                        manageOffsets(record, count, consumer);
                        count++;
                    }
                    consumer.commitAsync();
                }
            }
        } catch (WakeupException i) {
            log.error("WakeupException: {} : ", i.getMessage(), i);
        } catch (Exception e) {
            log.error("Aggregator: Ошибка чтения данных {} : ", e.getMessage(), e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем Kafka consumer и producer");
                consumer.close();
                producer.close(Duration.ofSeconds(5));
            }
        }
    }

    private void manageOffsets(ConsumerRecord<Long, SpecificRecordBase> record,
                                      int count, Consumer<Long, SpecificRecordBase> consumer) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

}