package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.service.handler.KafkaConsumerHandlers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {

        private final KafkaConsumerHandlers kafkaConsumerHandlers;
        private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
        private final KafkaClient kafkaClient;

        @Value("${kafka.consumer.user-properties.topic.user-action}")
        private String userActionTopic;
        @Value("${kafka.consumer.user-properties.user-consume-attempt-timeout}")
        private Duration timeout;

        @Override
        public void run() {
                Consumer<Long, UserActionAvro> consumer = kafkaClient.getUserActionConsumer();
                List<String> topic = List.of(userActionTopic);
                try {
                        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
                        consumer.subscribe(topic);
                        log.info("Подписка на user-action топик: {} ", topic);

                        while (true) {
                                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(timeout);
                                if (!records.isEmpty()) {
                                        try {
                                                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                                                        log.info("topic = {}, partition = {}, offset = {}, record = {}",
                                                                record.topic(), record.partition(), record.offset(), record.value());
                                                        kafkaConsumerHandlers.userActionGot(record.value());
                                                        log.info("UserAction событие обработано.");
                                                        currentOffsets.put(
                                                                new TopicPartition(record.topic(), record.partition()),
                                                                new OffsetAndMetadata(record.offset() + 1)
                                                        );
                                                }
                                                consumer.commitAsync((offsets, exception) -> {
                                                        if (exception != null) {
                                                                log.warn("Во время фиксации произошла ошибка. Офсет: {}", offsets, exception);
                                                        }
                                                });
                                        } catch (Exception e) {
                                                log.error("Ошибка обработки сообщений из топика {}: {}", topic, e.getMessage(), e);
                                        }
                                }
                        }
                } catch (WakeupException ignored) {
                } catch (Exception ex) {
                        log.error("Ошибка чтения данных из топика {}", topic);
                        log.error(ex.getMessage(), ex);
                } finally {
                        try {
                                consumer.commitSync();
                        } finally {
                                consumer.close(Duration.ofMillis(100));
                        }
                }
        }
}