package ru.yandex.practicum.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.stats.dashboard.RecommendationsControllerGrpc;
import ru.yandex.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.stats.event.RecommendedEventProto;
import ru.yandex.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.stats.event.UserPredictionsRequestProto;
import ru.yandex.practicum.service.handler.KafkaRecommendationHandlers;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final KafkaRecommendationHandlers kafkaRecommendationHandlers;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        kafkaRecommendationHandlers.getRecommendationsForUser(request, responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        kafkaRecommendationHandlers.getSimilarEvents(request, responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        kafkaRecommendationHandlers.getInteractionsCount(request, responseObserver);
    }

}
