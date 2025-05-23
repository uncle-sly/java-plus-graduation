package ru.yandex.practicum.feignClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/api/v1/user")
public interface UserClient extends UserOperationsClient {

}
