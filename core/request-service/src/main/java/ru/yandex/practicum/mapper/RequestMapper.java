package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.requests.ParticipationRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.utility.Constants;


@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "created", source = "createdOn", dateFormat = Constants.FORMAT_DATETIME)
    ParticipationRequestDto toParticipationRequestDto(Request request);
}