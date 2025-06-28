package ru.yandex.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.yandex.practicum.grpc.stats.event.UserActionProto;
import ru.yandex.practicum.mapper.UserActionMapper;
import ru.yandex.practicum.service.UserActionService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionService userActionService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {

        log.info("UserAction, получено событие: {}", request);

        try {
            // обработчик полученного события
            userActionService.collectUserAction(UserActionMapper.toUserActionAvro(request));
            // после обработки события возвращаем ответ клиенту
            responseObserver.onNext(Empty.getDefaultInstance());
            // и завершаем обработку запроса
            responseObserver.onCompleted();
        } catch (Exception e) {
            // в случае исключения отправляем ошибку клиенту
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
            log.error("UserAction, Ошибка обработки события: {} {}", request, e.getMessage(), e);
        }
        log.info("UserAction, обработано событие: {}", request);
    }

}

