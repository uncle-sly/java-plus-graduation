package ewm.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.stats.analizer.RecommendationsControllerGrpc;
import ru.yandex.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.stats.event.RecommendedEventProto;
import ru.yandex.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.stats.event.UserPredictionsRequestProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StatsAnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerClient;

    public List<RecommendedEventProto> getRecommendations(Long userId, Integer maxResults) {

        UserPredictionsRequestProto requestProto = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        log.info("поступил запрос на получение рекомендаций: {}", requestProto);

        List<RecommendedEventProto> recommendationsList = new ArrayList<>();

        analyzerClient.getRecommendationsForUser(requestProto)
                .forEachRemaining(recommendationsList::add);
        log.info("рекомендации получены: {}", recommendationsList);
        return recommendationsList;
    }

    public List<RecommendedEventProto> getSimilarEvents(Long eventId, Long userId, Integer maxResults) {
        SimilarEventsRequestProto requestProto = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        log.info("поступил запрос на получение схожих мероприятий: {}", requestProto);

        List<RecommendedEventProto> recommendationsList = new ArrayList<>();
        analyzerClient.getSimilarEvents(requestProto)
                .forEachRemaining(recommendationsList::add);
        log.info("схожие мероприятия получены: {}", recommendationsList);
        return recommendationsList;
    }

    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto requestProto = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        log.info("поступил запрос на получение суммы максимальных весов: {}", requestProto);
        Map<Long, Double> result = new HashMap<>();

        analyzerClient.getInteractionsCount(requestProto)
                .forEachRemaining(e -> result.put(e.getEventId(), e.getScore()));

        log.info("суммы максимальных весов мероприятий получены: {}", result);
        return result;
    }

}
