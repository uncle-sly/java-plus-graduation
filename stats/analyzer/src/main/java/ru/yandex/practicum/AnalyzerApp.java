package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.service.EventSimilarityProcessor;
import ru.yandex.practicum.service.UserActionProcessor;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);

        final EventSimilarityProcessor eventSimilarityProcessor = context.getBean(EventSimilarityProcessor.class);
              UserActionProcessor userActionProcessor = context.getBean(UserActionProcessor.class);

        // запускаем в отдельном потоке обработчик событий
        // от EventSimilarityProcessor
        Thread similarityThread = new Thread(eventSimilarityProcessor);
        similarityThread.setName("EventSimilarityProcessorThread");
        similarityThread.start();

        // В текущем потоке начинаем обработку
        // снимков состояния датчиков
        userActionProcessor.run();
    }

}