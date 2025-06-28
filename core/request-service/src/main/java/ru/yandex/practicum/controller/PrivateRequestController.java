package ru.yandex.practicum.controller;

import ewm.client.StatsCollectorClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateRequestController {

    private final RequestService requestService;
    private final StatsCollectorClient statsCollectorClient;

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        statsCollectorClient.collectEventRegistration(userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequest(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest eventRequest) {
        return requestService.updateStatusRequest(userId, eventId, eventRequest);
    }

}