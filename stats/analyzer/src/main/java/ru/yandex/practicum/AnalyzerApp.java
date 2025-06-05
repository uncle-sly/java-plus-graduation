package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.kafka.EventSimilarityConsumerService;
import ru.yandex.practicum.kafka.UserActionConsumerService;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);

        EventSimilarityConsumerService kafkaConsumer = context.getBean(EventSimilarityConsumerService.class);
        UserActionConsumerService userConsumer = context.getBean(UserActionConsumerService.class);

        Thread userConsumerThread = new Thread(userConsumer);
        userConsumerThread.setName("UserActionConsumerServiceThread");
        userConsumerThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaConsumer::stop));
        kafkaConsumer.start();
    }

}