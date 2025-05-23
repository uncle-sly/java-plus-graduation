package ewm.event.service;

import ewm.event.dto.*;
import ru.yandex.practicum.dto.event.EventFullDto;

import java.util.List;

public interface EventService {

    List<EventShortDto> getAllEvents(ReqParam reqParam);

    List<EventFullDto> getAllEvents(AdminEventParams params);

    EventFullDto publicGetEvent(long id);

    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size);

    EventFullDto findUserEventById(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    // feign
    EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId);

    EventFullDto findById(Long id);

    List<EventFullDto> findAllByInitiatorId(Long initiatorId);

    Boolean findEventsWithCategory(Long id);

    Boolean findEventWithInitiatorId(Long eventId, Long initiatorId);
}

