package ewm.event.mapper;

import ru.yandex.practicum.dto.event.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.NewEventDto;
import ewm.event.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.yandex.practicum.utility.Constants.FORMAT_DATETIME;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    List<EventShortDto> toEventShortDto(List<Event> event);

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "initiator.id", source = "initiatorId")
    @Mapping(target = "commentsCount", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    List<EventFullDto> toEventFullDtos(List<Event> events);

    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "eventDate", source = "eventDate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "location.id", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    List<EventShortDto> toEventShortDtos(List<EventFullDto> eventFullDtos);

    default LocalDateTime stringToLocalDateTime(String stringDate) {
        if (stringDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
        return LocalDateTime.parse(stringDate, formatter);
    }

    default String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
        return localDateTime.format(formatter);
    }
}
