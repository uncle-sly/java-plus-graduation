package ru.yandex.practicum.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.model.UserAction;
import ru.yandex.practicum.model.UserActionType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@UtilityClass
@Slf4j
public class UserActionMapper {

    public UserAction mapToUserAction(UserActionAvro userActionAvro) {
        return UserAction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .actionType(UserActionType.valueOf(userActionAvro.getActionType().name()))
                .timestamp(mapToTimestamp(userActionAvro.getTimestamp()))
                .build();

    }

    private LocalDateTime mapToTimestamp(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

}
