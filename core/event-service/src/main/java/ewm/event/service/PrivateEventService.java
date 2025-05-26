package ewm.event.service;

import ewm.event.dto.EventShortDto;
import ewm.event.dto.NewEventDto;
import ewm.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.dto.event.EventFullDto;

import java.util.List;

public interface PrivateEventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size);

    EventFullDto findUserEventById(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

}
