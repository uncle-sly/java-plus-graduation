package ru.yandex.practicum.feignClient;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.dto.requests.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestOperationsClient {

    @GetMapping("/count")
    Long countRequestsByEventAndStatus(@RequestParam Long eventId, @RequestParam RequestStatus status) throws FeignException;

    @GetMapping("/list")
    List<ParticipationRequestDto> findAllByEventIdInAndStatus(@RequestParam List<Long> ids, @RequestParam RequestStatus status) throws FeignException;

    @GetMapping("/find")
    Optional<ParticipationRequestDto> findByRequestIdAndEventId(@RequestParam Long requestId, @RequestParam Long eventId) throws FeignException;

}
