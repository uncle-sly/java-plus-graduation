package ru.yandex.practicum.service.handler;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.stats.event.RecommendedEventProto;
import ru.yandex.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.stats.event.UserPredictionsRequestProto;
import ru.yandex.practicum.model.EventSimilarity;
import ru.yandex.practicum.model.UserAction;
import ru.yandex.practicum.repository.EventSimilarityRepository;
import ru.yandex.practicum.repository.UserActionRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaRecommendationHandlersImpl implements KafkaRecommendationHandlers {

    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {

        List<UserAction> interactions = userActionRepository.findByUserId(request.getUserId());
        if (interactions.isEmpty()) {
            responseObserver.onCompleted();
            return;
        }
        Set<Long> recentEvents = interactions.stream()
                .sorted(Comparator.comparing(UserAction::getTimestamp).reversed())
                .limit(request.getMaxResults())
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        Set<Long> userInteractions = interactions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        List<EventSimilarity> similarities = new ArrayList<>();
        for (Long eventId : recentEvents) {
            similarities.addAll(eventSimilarityRepository.findByEventAOrEventB(eventId, eventId));
        }
        streamRecommendedEvents(similarities, userInteractions, request.getMaxResults(), responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        List<EventSimilarity> similarities = eventSimilarityRepository.findByEventAOrEventB(request.getEventId(), request.getEventId());

        Set<Long> userInteractions = userActionRepository.findByUserId(request.getUserId())
                .stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        streamRecommendedEvents(similarities, userInteractions, request.getMaxResults(), responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        for (long eventId : request.getEventIdList()) {
            double totalWeight = userActionRepository.findByEventId(eventId)
                    .stream()
                    .mapToDouble((ua) -> switch (ua.getActionType()) {
                        case VIEW -> 0.4;
                        case REGISTER -> 0.8;
                        case LIKE -> 1.0;
                    })
                    .sum();
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(totalWeight)
                    .build());
        }
        responseObserver.onCompleted();
    }

    private void streamRecommendedEvents(
            List<EventSimilarity> similarities,
            Set<Long> userInteractions,
            int limit,
            StreamObserver<RecommendedEventProto> responseObserver) {
        similarities.stream()
                .filter(es -> !userInteractions.contains(es.getEventA()) || !userInteractions.contains(es.getEventB()))
                .sorted(Comparator.comparing(EventSimilarity::getScore).reversed())
                .limit(limit)
                .forEach(es -> {
                    long recommendedEvent = userInteractions.contains(es.getEventA()) ? es.getEventB() : es.getEventA();
                    responseObserver.onNext(RecommendedEventProto.newBuilder()
                            .setEventId(recommendedEvent)
                            .setScore(es.getScore())
                            .build());
                });
        responseObserver.onCompleted();
    }

}