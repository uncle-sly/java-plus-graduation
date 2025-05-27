package ru.yandex.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service", path = "/api/v1/event", fallback = EventClientFallback.class)
public interface EventClient extends EventOperationsClient {

}
