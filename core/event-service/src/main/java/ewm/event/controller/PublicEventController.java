package ewm.event.controller;

import ewm.client.StatsCollectorClient;
import ewm.event.service.PublicEventService;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.dto.event.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.ReqParam;
import ewm.event.model.EventSort;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static ru.yandex.practicum.utility.Constants.FORMAT_DATETIME;
import static ru.yandex.practicum.utility.Constants.REQUEST_HEADER;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventService eventService;
    private final StatsCollectorClient statsCollectorClient;

    @GetMapping
    public List<EventShortDto> publicGetAllEvents(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeStart,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(required = false) EventSort sort,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size) {
        ReqParam reqParam = ReqParam.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        return eventService.getAllEvents(reqParam);
    }

    @GetMapping("/{id}")
    public EventFullDto publicGetEvent(@PathVariable long id, @RequestHeader(REQUEST_HEADER) long userId) {
        EventFullDto eventFullDto = eventService.publicGetEvent(id);
        statsCollectorClient.collectEventView(userId, id);
        return eventFullDto;
    }

    @GetMapping("/recommendations")
    public List<EventFullDto> publicGetRecomendations(@RequestHeader(REQUEST_HEADER) long userId, @RequestParam int limit) {
        log.info("запрос рекомендованных мероприятий: {} с лимитом {}", userId, limit);
        return eventService.publicGetRecomendations(userId, limit);
    }

    @PutMapping("/{eventId}/like")
    public void publicSendLikeToCollector(@PathVariable long eventId, @RequestHeader(REQUEST_HEADER) long userId) {
        log.info("отправка like мероприятия {} от пользователя {} в Collector", eventId, userId);
        eventService.publicSendLikeToCollector(userId, eventId);
    }

}