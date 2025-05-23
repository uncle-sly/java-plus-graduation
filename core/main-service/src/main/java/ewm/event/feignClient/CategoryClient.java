package ewm.event.feignClient;


import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.feignClient.CategoryOperationsClient;

@FeignClient(name = "category-service", path = "/api/v1/category")
public interface CategoryClient extends CategoryOperationsClient {

}
