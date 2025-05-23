package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.event.EventState;
import ru.yandex.practicum.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.dto.requests.RequestStatus;
import feign.FeignException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.feignClient.EventClient;
import ru.yandex.practicum.feignClient.UserClient;
import ru.yandex.practicum.mapper.RequestMapper;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserClient userClient;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventClient eventClient;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        List<Request> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {

        if (findEventWithInitiatorId(eventId, userId)) {
            throw new InitiatorRequestException("Пользователь с ID - " + userId + ", является инициатором события с ID - " + eventId + ".");
        }

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new RepeatUserRequestorException("Пользователь с ID - " + userId + ", уже заявился на событие с ID - " + eventId + ".");
        }
        EventFullDto event = findById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotPublishEventException("Данное событие ещё не опубликовано");
        }

        Request request = new Request();
        request.setRequesterId(findUser(userId).getId());
        request.setEventId(event.getId());

        Long confirmedRequests = requestRepository.countRequestsByEventAndStatus(event.getId(), RequestStatus.CONFIRMED);
        if (confirmedRequests >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new ParticipantLimitException("Достигнут лимит участников для данного события.");
        }

        request.setCreatedOn(LocalDateTime.now());
        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            return requestMapper.toParticipationRequestDto(requestRepository.save(request));
        }

        if (event.getRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
            return requestMapper.toParticipationRequestDto(requestRepository.save(request));
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request cancelRequest = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Request.class, "Запрос с ID - " + requestId + ", не найден."));
        cancelRequest.setStatus(RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(requestRepository.save(cancelRequest));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        List<EventFullDto> userEvents = findAllByInitiatorId(userId);

        EventFullDto event = userEvents.stream()
                .filter(e -> e.getInitiator().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь с ID - " + userId + ", не является инициатором события с ID - " + eventId + "."));
        return requestRepository.findByEventId(event.getId()).stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventRequest) {
        EventFullDto event = findByIdAndInitiatorId(eventId, userId);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new OperationUnnecessaryException("Запрос составлен некорректно.");
        }

        List<Long> requestIds = eventRequest.getRequestIds();
        List<Request> requests = requestIds.stream()
                .map(r -> requestRepository.findByIdAndEventId(r, eventId)
                        .orElseThrow(() -> new ValidationException("Запрос с ID - " + r + ", не найден.")))
                .toList();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        Long confirmedRequestsCount = requestRepository.countRequestsByEventAndStatus(event.getId(), RequestStatus.CONFIRMED);

        if (confirmedRequestsCount >= event.getParticipantLimit()) {
            throw new ParticipantLimitException("Достигнут лимит участников для данного события.");
        }

        List<Request> updatedRequests = new ArrayList<>();

        for (Request request : requests) {
            if (request.getStatus().equals(RequestStatus.PENDING)) {
                if (eventRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
                    if (confirmedRequestsCount <= event.getParticipantLimit()) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        updatedRequests.add(request);
                        confirmedRequestsCount++;
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        updatedRequests.add(request);
                    }
                } else {
                    request.setStatus(eventRequest.getStatus());
                    updatedRequests.add(request);
                }
            }
        }

        List<Request> savedRequests = requestRepository.saveAll(updatedRequests);
        for (Request request : savedRequests) {
            if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
            } else {
                rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
            }
        }

        EventRequestStatusUpdateResult resultRequest = new EventRequestStatusUpdateResult();
        resultRequest.setConfirmedRequests(confirmedRequests);
        resultRequest.setRejectedRequests(rejectedRequests);
        return resultRequest;
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> ids, RequestStatus status) {
        return requestRepository.findAllByEventIdInAndStatus(ids, status)
                .stream().map(requestMapper::toParticipationRequestDto).toList();
    }

    @Override
    public Long countRequestsByEventAndStatus(Long eventId, RequestStatus status) {
        return requestRepository.countRequestsByEventAndStatus(eventId, status);
    }

    @Override
    public Optional<ParticipationRequestDto> findByRequestIdAndEventId(Long requestId, Long eventId) {
        return Optional.ofNullable(requestMapper.toParticipationRequestDto(requestRepository.findByRequesterIdAndEventId(requestId, eventId)
                .orElseThrow(() -> new EntityNotFoundException(Request.class, " не найден."))));
    }

    private UserDto findUser(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException e) {
            throw new EntityNotFoundException(UserDto.class, "Пользователь c ID - " + userId + ", не найден.");
        }
    }

    private EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId) {
        try {
            return eventClient.findByIdAndInitiatorId(eventId, initiatorId);
        } catch (FeignException e) {
            throw new InitiatorRequestException("initiator с ID - " + initiatorId + ", не найден.");
        }
    }

    private Boolean findEventWithInitiatorId(Long eventId, Long initiatorId) {
        try {
            return eventClient.findEventWithInitiatorId(eventId, initiatorId);
        } catch (FeignException e) {
            throw new EntityNotFoundException(EventFullDto.class, "Событие с ID - " + eventId + ", не найдено.");
        }
    }

    private EventFullDto findById(Long eventId) {
        try {
            return eventClient.findById(eventId);
        } catch (FeignException e) {
            throw new EntityNotFoundException(EventFullDto.class, "Событие с ID - " + eventId + ", не найдено.");
        }
    }

    private List<EventFullDto> findAllByInitiatorId(Long initiatorId) {
        try {
            return eventClient.findAllByInitiatorId(initiatorId);
        } catch (FeignException e) {
            throw new EntityNotFoundException(EventFullDto.class, "Событие с userId - " + initiatorId + ", не найдено.");
        }
    }

}