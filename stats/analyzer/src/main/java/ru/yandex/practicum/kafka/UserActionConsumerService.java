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
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.service.handler.KafkaConsumerHandlers;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumerService implements Runnable {
    private final KafkaConsumerHandlers kafkaConsumerService;
    private final KafkaConfiguration kafkaConfiguration;
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<String, UserActionAvro> consumer;


    public void run() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
            consumer.subscribe(kafkaConfiguration.getUserTopics());
            log.info("Подписка на UserAction топик: {} ", kafkaConfiguration.getUserTopics());

            while (true) {
                ConsumerRecords<String, UserActionAvro> actionRecords = consumer.poll(kafkaConfiguration.getConsumeAttemptTimeout());
                int countAction = 0;
                for (ConsumerRecord<String, UserActionAvro> record : actionRecords) {
                    log.info("topic = {}, partition = {}, offset = {}, record = {}",
                            record.topic(), record.partition(), record.offset(), record.value());
                    kafkaConsumerService.userActionGot(record.value());
                    log.info("UserAction событие обработано.");

                    manageOffsets(record, countAction, consumer);
                    countAction++;
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

    private static void manageOffsets(ConsumerRecord<String, UserActionAvro> record, int count,
                                      KafkaConsumer<String, UserActionAvro> consumer) {
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