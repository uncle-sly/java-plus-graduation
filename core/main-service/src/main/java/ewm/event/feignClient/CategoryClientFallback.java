package ewm.event.feignClient;

import ewm.exception.EntityNotFoundException;
import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.category.CategoryDto;

import java.util.List;

@Component
public class CategoryClientFallback implements CategoryClient {

    @Override
    public CategoryDto getCategoryById(Long id) throws FeignException {
        throw new EntityNotFoundException(CategoryClientFallback.class,
                "Категория с ID: " + id + "не обнаружена.");
    }

    @Override
    public List<CategoryDto> getCategorysList(List<Long> ids) throws FeignException {
        return List.of();
    }

}
