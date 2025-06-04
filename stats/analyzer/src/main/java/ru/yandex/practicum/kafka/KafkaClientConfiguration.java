package ru.yandex.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;


@Configuration
@Slf4j
public class KafkaClientConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "kafka.consumer.user-properties")
    public Properties getEventSimilarityConsumerProperties() {
        log.info("{}: Наполняем UserAction Properties", KafkaClientConfiguration.class.getSimpleName());
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka.consumer.similarity-properties")
    public Properties getUserActionConsumerProperties() {
        log.info("{}: Наполняем EventSimilarity Properties", KafkaClientConfiguration.class.getSimpleName());
        return new Properties();
    }


    @Bean
    KafkaClient getKafkaClient() {
        return new KafkaClient() {
            private Consumer<Long, EventSimilarityAvro> eventSimilarityConsumer;
            private Consumer<Long, UserActionAvro> userActionConsumer;

            @Override
            public Consumer<Long, EventSimilarityAvro> getEventSimilarityConsumer() {
                log.info("{}: Создание EventSimilarityConsumer", KafkaClientConfiguration.class.getSimpleName());
                eventSimilarityConsumer = new KafkaConsumer<>(getEventSimilarityConsumerProperties());
                return eventSimilarityConsumer;
            }

            @Override
            public Consumer<Long, UserActionAvro> getUserActionConsumer() {
                log.info("{}: Создание UserActionConsumer", KafkaClientConfiguration.class.getSimpleName());
                userActionConsumer = new KafkaConsumer<>(getUserActionConsumerProperties());
                return userActionConsumer;
            }

            @Override
            public void close() throws Exception {
                try {
                    eventSimilarityConsumer.commitSync();
                    userActionConsumer.commitSync();
                } finally {
                    log.info("{}: Закрытие EventSimilarityConsumer", KafkaClientConfiguration.class.getSimpleName());
                    eventSimilarityConsumer.close();
                    log.info("{}: Закрытие UserActionConsumer", KafkaClientConfiguration.class.getSimpleName());
                    userActionConsumer.close();
                }
            }
        };
    }

}
