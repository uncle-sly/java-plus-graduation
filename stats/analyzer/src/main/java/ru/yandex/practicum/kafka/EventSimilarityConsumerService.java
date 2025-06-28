package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.service.handler.KafkaConsumerHandlers;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityConsumerService {
    private final KafkaConsumerHandlers kafkaConsumerService;
    private final KafkaConfiguration kafkaConfiguration;
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<String, EventSimilarityAvro> consumer;


    public void start() {
        try {
            consumer.subscribe(kafkaConfiguration.getSimilarityTopics());
            log.info("Подписка на similarity топик: {} ", kafkaConfiguration.getSimilarityTopics());

            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(kafkaConfiguration.getConsumeAttemptTimeout());
                int countSimilarity = 0;
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    log.info("topic = {}, partition = {}, offset = {}, record = {}",
                            record.topic(), record.partition(), record.offset(), record.value());
                    kafkaConsumerService.eventsSimilarityGot(record.value());
                    log.info("Similarity событие обработано.");

                    manageOffsets(record, countSimilarity, consumer);
                    countSimilarity++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (
                Exception e) {
            log.error("Ошибка чтения данных {} : ", e.getMessage(), e);

        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, EventSimilarityAvro> record, int count,
                                      KafkaConsumer<String, EventSimilarityAvro> consumer) {
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

    public void stop() {
        consumer.wakeup();
    }

}