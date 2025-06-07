package ru.yandex.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Properties;


@Configuration
@Slf4j
public class KafkaClientConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "kafka.consumer.properties")
    public Properties getConsumerProperties() {
        log.info("{}: Наполняем Consumer Properties", KafkaClientConfiguration.class.getSimpleName());
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka.producer.properties")
    public Properties getProducerProperties() {
        log.info("{}: Наполняем Producer Properties", KafkaClientConfiguration.class.getSimpleName());
        return new Properties();
    }

    @Bean
    KafkaClient getKafkaClient() {
        return new KafkaClient() {
            private Consumer<Long, SpecificRecordBase> kafkaConsumer;
            private Producer<Long, SpecificRecordBase> kafkaProducer;

            @Override
            public Producer<Long, SpecificRecordBase> getProducer() {
                log.info("{}: Создание kafkaProducer", KafkaClientConfiguration.class.getSimpleName());
                kafkaProducer = new KafkaProducer<>(getProducerProperties());
                return kafkaProducer;
            }

            @Override
            public Consumer<Long, SpecificRecordBase> getConsumer() {
                log.info("{}: Создание kafkaConsumer", KafkaClientConfiguration.class.getSimpleName());
                kafkaConsumer = new KafkaConsumer<>(getConsumerProperties());
                return kafkaConsumer;
            }

            @Override
            public ProducerRecord<Long, SpecificRecordBase> getProducerRecord(String topic, SpecificRecordBase record) {
                return new ProducerRecord<>(topic, record);
            }

            @Override
            public void close() {
                try {
                    kafkaProducer.flush();
                    kafkaConsumer.commitSync();
                } finally {
                    log.info("Закрываем Kafka consumer и producer");
                    kafkaProducer.close(Duration.ofMillis(100));
                    kafkaConsumer.close();
                }
            }
        };
    }
}