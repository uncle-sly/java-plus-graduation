package ru.yandex.practicum.dto;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.dto.requests.ParticipationRequestDto;

import java.util.List;

@Getter
@Setter
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;
}