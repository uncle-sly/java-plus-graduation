package ewm.event.service;

import ewm.event.dto.*;
import ru.yandex.practicum.dto.event.EventFullDto;

import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getAllEvents(ReqParam reqParam);

    EventFullDto publicGetEvent(long id);

}
