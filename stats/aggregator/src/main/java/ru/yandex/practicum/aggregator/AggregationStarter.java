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
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий
     */
    public void start() {
        Producer<Long, SpecificRecordBase> kafkaProducer = kafkaClient.getProducer();
        Consumer<Long, SpecificRecordBase> kafkaConsumer = kafkaClient.getConsumer();
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaConsumer::wakeup));

        try {
            kafkaConsumer.subscribe(List.of(consumerTopic));

            while (true) {
                ConsumerRecords<Long, SpecificRecordBase> records = kafkaConsumer.poll(pollTimeout);
                if (!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<Long, SpecificRecordBase> record : records) {
                        log.info("Получено сообщение: {}", record.value());
                        List<EventSimilarityAvro> eventsSimilarity = aggregatorService.processUserActions(record.value());
                        eventsSimilarity.forEach(similarity -> {
                            log.info("Отправляем результат: {}", similarity);
                            kafkaProducer.send(kafkaClient.getProducerRecord(producerTopic, similarity), (metadata, ex) -> {
                                if (ex != null) {
                                    log.error("Ошибка при отправке сообщения: {}", similarity, ex);
                                }
                            });
                        });
                        manageOffsets(record, count, kafkaConsumer);
                        count++;
                    }
                    kafkaConsumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("{}: Ошибка во время обработки событий от датчиков", AggregationStarter.class.getSimpleName(), e);
        } finally {
            try {
                kafkaProducer.flush();
                kafkaConsumer.commitSync();
            } finally {
                log.info("Закрываем Kafka consumer и producer");
                kafkaConsumer.close();
                kafkaProducer.close(Duration.ofSeconds(5));
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<Long, SpecificRecordBase> record, int count, Consumer<Long, SpecificRecordBase> consumer) {
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