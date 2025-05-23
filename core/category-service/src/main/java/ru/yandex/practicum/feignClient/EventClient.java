package ru.yandex.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "main-service", path = "/api/v1/event")
public interface EventClient extends EventOperationsClient {

}
