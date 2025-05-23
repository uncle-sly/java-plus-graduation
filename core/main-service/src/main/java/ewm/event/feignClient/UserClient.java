package ewm.event.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feignClient.UserOperationsClient;

@FeignClient(name = "user-service", path = "/api/v1/user")
public interface UserClient extends UserOperationsClient {

}
