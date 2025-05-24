package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.dto.requests.RequestStatus;
import ru.yandex.practicum.feignClient.RequestOperationsClient;
import ru.yandex.practicum.service.RequestService;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/request")
@Slf4j
public class RequestClientController implements RequestOperationsClient {

    private final RequestService requestService;

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> ids, RequestStatus status) {
        return requestService.findAllByEventIdInAndStatus(ids, status);
    }

    @Override
    public Long countRequestsByEventAndStatus(Long eventId, RequestStatus status) {
        return requestService.countRequestsByEventAndStatus(eventId, status);
    }

    @Override
    public Optional<ParticipationRequestDto> findByRequestIdAndEventId(Long requestId, Long eventId) {
       return requestService.findByRequestIdAndEventId(requestId, eventId);
    }


}
