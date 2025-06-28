package ewm.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.stats.collector.UserActionControllerGrpc;
import ru.yandex.practicum.grpc.stats.event.ActionTypeProto;
import ru.yandex.practicum.grpc.stats.event.UserActionProto;

import java.time.Instant;

@Slf4j
@Service
public class StatsCollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    private void sendUserAction(Long userId, Long eventId, ActionTypeProto actionType) {

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();
        log.info("передача действия пользователя {}", request);
        collectorClient.collectUserAction(request);
    }

    public void collectEventView(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void collectEventLike(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    public void collectEventRegistration(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

}
