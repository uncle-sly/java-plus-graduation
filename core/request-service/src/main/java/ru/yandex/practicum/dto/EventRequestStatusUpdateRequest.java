package ru.yandex.practicum.dto;

import lombok.Getter;
import ru.yandex.practicum.dto.requests.RequestStatus;

import java.util.List;

@Getter
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}