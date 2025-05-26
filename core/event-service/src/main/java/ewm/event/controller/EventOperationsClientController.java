package ewm.event.controller;

import ru.yandex.practicum.dto.event.EventFullDto;
import ewm.event.service.InternalEventService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.feignClient.EventOperationsClient;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class EventOperationsClientController implements EventOperationsClient {

    private final InternalEventService eventService;

    @Override
    public EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId) throws FeignException {
        return eventService.findByIdAndInitiatorId(eventId, initiatorId);
    }

    @Override
    public EventFullDto findById(Long id) throws FeignException {
        return eventService.findById(id);
    }

    @Override
    public List<EventFullDto> findAllByInitiatorId(Long initiatorId) throws FeignException {
        return eventService.findAllByInitiatorId(initiatorId);
    }

    @Override
    public Boolean findEventsWithCategory(Long id) throws FeignException {
        return eventService.findEventsWithCategory(id);
    }

    @Override
    public Boolean findEventWithInitiatorId(Long eventId, Long initiatorId) throws FeignException {
        return eventService.findEventWithInitiatorId(eventId, initiatorId);
    }

}
