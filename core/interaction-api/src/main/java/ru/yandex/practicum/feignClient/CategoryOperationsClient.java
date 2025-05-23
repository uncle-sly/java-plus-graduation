package ru.yandex.practicum.feignClient;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.category.CategoryDto;

import java.util.List;

public interface CategoryOperationsClient {

    @GetMapping
    CategoryDto getCategoryById(@RequestParam Long id) throws FeignException;

    @GetMapping("/list")
    List<CategoryDto> getCategorysList(@RequestParam List<Long> ids) throws FeignException;

}
