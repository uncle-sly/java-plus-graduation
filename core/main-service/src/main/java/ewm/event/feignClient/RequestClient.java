package ewm.event.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feignClient.RequestOperationsClient;

@FeignClient(name = "request-service", path = "/api/v1/request", fallback = RequestClientFallback.class)
public interface RequestClient extends RequestOperationsClient {

}
