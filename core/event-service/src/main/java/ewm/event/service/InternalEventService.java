package ewm.event.service;

import ru.yandex.practicum.dto.event.EventFullDto;

import java.util.List;

public interface InternalEventService {

    EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId);

    EventFullDto findById(Long id);

    List<EventFullDto> findAllByInitiatorId(Long initiatorId);

    Boolean findEventsWithCategory(Long id);

    Boolean findEventWithInitiatorId(Long eventId, Long initiatorId);

}

