package ewm.event.service;

import ewm.event.dto.AdminEventParams;
import ewm.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.dto.event.EventFullDto;

import java.util.List;

public interface AdminEventService {

    List<EventFullDto> getAllEvents(AdminEventParams params);

    EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

}
