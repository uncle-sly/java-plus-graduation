package ewm.event.service;

import ewm.ParamDto;
import ewm.client.RestStatClient;
import ewm.comment.repository.CommentRepository;
import ewm.event.dto.*;
import ewm.event.feignClient.CategoryClient;
import ewm.event.feignClient.RequestClient;
import ewm.event.feignClient.UserClient;
import ewm.event.mapper.EventMapper;
import ewm.event.model.*;
import ewm.event.repository.EventRepository;
import ewm.event.repository.LocationRepository;
import ewm.exception.ConditionNotMetException;
import ewm.exception.EntityNotFoundException;
import ewm.exception.InitiatorRequestException;
import ewm.exception.ValidationException;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.event.EventState;
import ru.yandex.practicum.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.dto.requests.RequestStatus;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.user.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.utility.Constants.FORMAT_DATETIME;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements InternalEventService, PublicEventService, PrivateEventService, AdminEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RestStatClient statClient;
    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final LocationRepository locationRepository;
    private final RequestClient requestClient;
    private final CommentRepository commentRepository;

    @Override
    public List<EventShortDto> getAllEvents(ReqParam reqParam) {
        Pageable pageable = PageRequest.of(reqParam.getFrom(), reqParam.getSize());

        if (reqParam.getRangeStart() == null || reqParam.getRangeEnd() == null) {
            reqParam.setRangeStart(LocalDateTime.now());
            reqParam.setRangeEnd(LocalDateTime.now().plusYears(1));
        }
        List<EventFullDto> eventFullDtos = eventMapper.toEventFullDtos(eventRepository.findEvents(
                reqParam.getText(),
                reqParam.getCategories(),
                reqParam.getPaid(),
                reqParam.getRangeStart(),
                reqParam.getRangeEnd(),
                reqParam.getOnlyAvailable(),
                pageable
        ));
        if (eventFullDtos.isEmpty()) {
            throw new ValidationException(ReqParam.class, " События не найдены");
        }
        List<Long> eventsIds = eventFullDtos.stream().map(EventFullDto::getId).toList();
        List<EventCommentCount> eventCommentCountList = commentRepository.findAllByEventIds(eventsIds);

        eventFullDtos.forEach(eventFullDto ->
                eventFullDto.setCommentsCount(eventCommentCountList.stream()
                        .filter(eventComment -> eventComment.getEventId().equals(eventFullDto.getId()))
                        .map(EventCommentCount::getCommentCount)
                        .findFirst()
                        .orElse(0L)
                )
        );

        List<EventShortDto> addedViewsAndRequests = eventMapper.toEventShortDtos(addRequests(addViews(eventFullDtos)));

        if (reqParam.getSort() != null) {
            return switch (reqParam.getSort()) {
                case EVENT_DATE ->
                        addedViewsAndRequests.stream().sorted(Comparator.comparing(EventShortDto::getEventDate)).toList();
                case VIEWS ->
                        addedViewsAndRequests.stream().sorted(Comparator.comparing(EventShortDto::getViews)).toList();
            };
        }
        return addedViewsAndRequests;
    }

    @Override
    public List<EventFullDto> getAllEvents(AdminEventParams params) {
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize());

        if (params.getRangeStart() == null || params.getRangeEnd() == null) {
            params.setRangeStart(LocalDateTime.now());
            params.setRangeEnd(LocalDateTime.now().plusYears(1));
        }

        List<EventFullDto> eventFullDtos = eventMapper.toEventFullDtos(eventRepository.findAdminEvents(
                params.getUsers(),
                params.getStates(),
                params.getCategories(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable));

        return addRequests(addViews(eventFullDtos));
    }

    @Override
    public EventFullDto publicGetEvent(long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие c ID - " + id + ", не найдено."));
        if (event.getState() != EventState.PUBLISHED) {
            throw new EntityNotFoundException(Event.class, " Событие c ID - " + id + ", ещё не опубликовано.");
        }
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setCommentsCount(commentRepository.countCommentByEvent_Id(event.getId()));
        return addRequests(addViews(eventFullDto));
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(),
                DateTimeFormatter.ofPattern(FORMAT_DATETIME));
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(NewEventDto.class, "До начала события осталось меньше двух часов");
        }
        UserDto initiator = findUser(userId);

        CategoryDto category = findCategory(newEventDto.getCategory());

        Event event = eventMapper.toEvent(newEventDto);
        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0L);
        }
        event.setInitiatorId(initiator.getId());
        event.setCategoryId(category.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setLocation(locationRepository.save(event.getLocation()));
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " c ID = " + eventId + ", не найдено."));

        if (updateEventAdminRequest.getEventDate() != null) {
            if ((event.getPublishedOn() != null) && updateEventAdminRequest.getEventDate().isAfter(event.getPublishedOn().minusHours(1))) {
                throw new ConditionNotMetException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }
        if (updateEventAdminRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
            throw new ConditionNotMetException("Cобытие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        if (updateEventAdminRequest.getStateAction() == AdminStateAction.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
            throw new ConditionNotMetException("Cобытие можно отклонить, только если оно еще не опубликовано");
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
                event.setState(EventState.CANCELED);
            }
        }

        checkEvent(event, updateEventAdminRequest);
        return eventMapper.toEventFullDto(eventRepository.save(event));

    }

    @Override
    public List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size) {
        findUser(userId);
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        List<EventFullDto> eventFullDtos = eventMapper.toEventFullDtos(events);
        return eventMapper.toEventShortDtos(addRequests(addViews(eventFullDtos)));
    }

    @Override
    public EventFullDto findUserEventById(Long userId, Long eventId) {
        findUser(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие не найдено"));
        EventFullDto result = eventMapper.toEventFullDto(event);
        return addRequests(addViews(result));
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        findUser(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие не найдено"));
        if (event.getState() == EventState.PUBLISHED) {
            throw new InitiatorRequestException("Нельзя отредактировать опубликованное событие");
        }

        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(NewEventDto.class, "До начала события осталось меньше двух часов");
            }
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == PrivateStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
            if (updateRequest.getStateAction() == PrivateStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            }
        }

        checkEvent(event, updateRequest);
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " c ID = " + id + ", не найдено."));
        EventFullDto result = eventMapper.toEventFullDto(event);
        return addRequests(addViews(result));
    }

    @Override
    public EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, initiatorId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие не найдено"));
        EventFullDto result = eventMapper.toEventFullDto(event);
        return addRequests(addViews(result));
    }

    @Override
    public List<EventFullDto> findAllByInitiatorId(Long initiatorId) {
        List<Event> events = eventRepository.findAllByInitiatorId(initiatorId);
        List<EventFullDto> result = eventMapper.toEventFullDtos(events);
        return addViews(result);

    }

    @Override
    public Boolean findEventsWithCategory(Long id) {
        return !eventRepository.findAllByCategoryId(id).isEmpty();
    }

    @Override
    public Boolean findEventWithInitiatorId(Long eventId, Long initiatorId) {
        return eventRepository.findByIdAndInitiatorId(eventId, initiatorId).isPresent();
    }


    private List<EventFullDto> addViews(List<EventFullDto> eventDtos) {
        HashMap<String, EventFullDto> eventDtoMap = new HashMap<>();
        List<String> gettingUris = new ArrayList<>();
        LocalDateTime earlyPublishDate = LocalDateTime.now().minusHours(1);
        for (EventFullDto dto : eventDtos) {
            String uri = "/events/" + dto.getId();
            eventDtoMap.put(uri, dto);
            gettingUris.add(uri);
            if (dto.getPublishedOn() != null) {
                LocalDateTime dtoPublishDate = LocalDateTime.parse(dto.getPublishedOn(),
                        DateTimeFormatter.ofPattern(FORMAT_DATETIME));
                if (dtoPublishDate.isBefore(earlyPublishDate)) {
                    earlyPublishDate = dtoPublishDate;
                }
            }
        }
        ParamDto paramDto = new ParamDto(earlyPublishDate, LocalDateTime.now(), gettingUris, true);
        statClient.getStat(paramDto)
                .stream()
                .peek(viewStats -> eventDtoMap.get(viewStats.getUri()).setViews(viewStats.getHits()));
        return eventDtoMap.values().stream().toList();
    }

    private EventFullDto addViews(EventFullDto dto) {
        String uri = ("/events/" + dto.getId());
        LocalDateTime publishDate = LocalDateTime.now().minusHours(1);
        if (dto.getPublishedOn() != null) {
            publishDate = LocalDateTime.parse(dto.getPublishedOn(),
                    DateTimeFormatter.ofPattern(FORMAT_DATETIME));
        }
        ParamDto paramDto = new ParamDto(publishDate, LocalDateTime.now(), Collections.singletonList(uri), true);
        Long views = (long) statClient.getStat(paramDto).size();
        dto.setViews(views);
        return dto;
    }

    private void checkEvent(Event event, UpdateEventBaseRequest updateRequest) {
        if (updateRequest.getAnnotation() != null && !updateRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            CategoryDto category = findCategory(updateRequest.getCategory());
            event.setCategoryId(category.getId());
        }
        if (updateRequest.getDescription() != null && !updateRequest.getDescription().isBlank()) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            Optional<Location> locationOpt = locationRepository.findByLatAndLon(
                    updateRequest.getLocation().getLat(),
                    updateRequest.getLocation().getLon());
            Location location = locationOpt.orElse(locationRepository.save(
                    new Location(null, updateRequest.getLocation().getLat(), updateRequest.getLocation().getLon())));
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit().longValue());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    private List<EventFullDto> addRequests(List<EventFullDto> eventDtos) {
        List<Long> eventIds = eventDtos.stream().map(EventFullDto::getId).toList();
        List<ParticipationRequestDto> requests = findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> requestsMap = requests.stream()
                .collect(Collectors.groupingBy(ParticipationRequestDto::getEvent, Collectors.counting()));
        eventDtos.forEach(eventDto -> eventDto.setConfirmedRequests(requestsMap.getOrDefault(eventDto.getId(), 0L)));
        return eventDtos;
    }

    private EventFullDto addRequests(EventFullDto eventDto) {
        eventDto.setConfirmedRequests(
                requestClient.countRequestsByEventAndStatus(eventDto.getId(), RequestStatus.CONFIRMED)
        );
        return eventDto;
    }

    private UserDto findUser(Long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException e) {
            throw new EntityNotFoundException(UserDto.class, "Пользователь c ID - " + userId + ", не найден.");
        }
    }

    private CategoryDto findCategory(Long categoryId) {
        try {
            return categoryClient.getCategoryById(categoryId);
        } catch (FeignException e) {
            throw new EntityNotFoundException(CategoryDto.class, "Категория c ID - " + categoryId + " не найдена");
        }
    }

    private List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> ids, RequestStatus status) {
        try {
            return requestClient.findAllByEventIdInAndStatus(ids, status);
        } catch (FeignException e) {
            throw new EntityNotFoundException(CategoryDto.class, "Запросы - не найдены");
        }
    }

}