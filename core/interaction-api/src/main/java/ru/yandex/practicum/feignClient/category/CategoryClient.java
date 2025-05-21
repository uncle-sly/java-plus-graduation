package ru.yandex.practicum.feignClient.category;


import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.category.CategoryDto;

import java.util.List;

@FeignClient(name = "category-service", path = "/api/v1/category")
public interface CategoryClient {

    @GetMapping
    CategoryDto getCategoryById(@RequestParam Long id) throws FeignException;

    @GetMapping("/list")
    List<CategoryDto> getCategorysList(@RequestParam List<Long> ids) throws FeignException;

}
