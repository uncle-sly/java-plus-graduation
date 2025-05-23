package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.dto.requests.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequest);

//    feign
    List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> ids, RequestStatus status);

    Long countRequestsByEventAndStatus(Long eventId, RequestStatus status);

    Optional<ParticipationRequestDto> findByRequestIdAndEventId(Long requestId, Long eventId);
}