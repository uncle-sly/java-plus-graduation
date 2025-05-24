package ru.yandex.practicum.feignClient;


import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.event.EventFullDto;

import java.util.List;

public interface EventOperationsClient {

    @GetMapping("/initiator")
    EventFullDto findByIdAndInitiatorId(@RequestParam Long eventId, @RequestParam Long initiatorId) throws FeignException;

    @GetMapping("/{id}")
    EventFullDto findById(@PathVariable Long id) throws FeignException;

    @GetMapping("/list")
    List<EventFullDto> findAllByInitiatorId(@RequestParam Long initiatorId) throws FeignException;

    @GetMapping("/category")
    Boolean findEventsWithCategory(@RequestParam Long id) throws FeignException;

    @GetMapping("/event")
    Boolean findEventWithInitiatorId(@RequestParam Long eventId, @RequestParam Long initiatorId) throws FeignException;

}