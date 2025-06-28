package ru.yandex.practicum.service.handler;

import io.grpc.stub.StreamObserver;
import ru.yandex.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.stats.event.RecommendedEventProto;
import ru.yandex.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.stats.event.UserPredictionsRequestProto;

public interface KafkaRecommendationHandlers {

    void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver);

    void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver);

    void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver);

}
