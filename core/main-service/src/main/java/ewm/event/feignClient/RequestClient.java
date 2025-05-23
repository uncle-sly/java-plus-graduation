package ewm.event.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feignClient.RequestOperationsClient;

@FeignClient(name = "request-service", path = "/api/v1/request")
public interface RequestClient extends RequestOperationsClient {

}
