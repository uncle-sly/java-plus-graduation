package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.kafka.KafkaProducer;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionServiceImpl implements UserActionService {

    @Value("${kafka.topic.user-action}")
    private String userActionTopic;

    private final KafkaProducer kafkaProducer;

    @Override
    public void collectUserAction(UserActionAvro userActionAvro) {
        log.info("Collecting user action: {}", userActionAvro);
        kafkaProducer.getProducer().send(kafkaProducer.getProducerRecord(userActionTopic, userActionAvro));
        log.info("Collected user action: {}", userActionAvro);
        log.info("UserAction Kafka_topic: {}", userActionTopic);
        log.info("User Action: {}", userActionAvro);
    }

}
