package ru.yandex.practicum.feignClient;

import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.exception.EntityNotFoundException;

import java.util.List;

@Component
public class EventClientFallback implements EventClient {

    @Override
    public EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId) throws FeignException {
        throw new EntityNotFoundException(EventClientFallback.class,
                "Событие с ID: " + eventId + ", Инициатор с ID: " + initiatorId + "не обнаружено.");
    }

    @Override
    public EventFullDto findById(Long id) throws FeignException {
        throw new EntityNotFoundException(EventClientFallback.class,
                "Событие с ID: " + id + "не обнаружено.");
    }

    @Override
    public List<EventFullDto> findAllByInitiatorId(Long initiatorId) throws FeignException {
        return List.of();
    }

    @Override
    public Boolean findEventsWithCategory(Long id) throws FeignException {
        return Boolean.FALSE;
    }

    @Override
    public Boolean findEventWithInitiatorId(Long eventId, Long initiatorId) throws FeignException {
        return Boolean.FALSE;
    }

}