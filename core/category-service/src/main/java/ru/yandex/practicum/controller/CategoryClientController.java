package ru.yandex.practicum.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.feignClient.category.CategoryClient;
import ru.yandex.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
@Slf4j
public class CategoryClientController implements CategoryClient {

    private final CategoryService categoryService;

    @Override
    public CategoryDto getCategoryById(Long id) throws FeignException {
        log.info("get category by id: {}", id);
        return categoryService.getCategoryById(id);
    }

    @Override
    public List<CategoryDto> getCategorysList(List<Long> ids) throws FeignException {
        log.info("get categorys list: {}", ids);
        return categoryService.getCategorysList(ids);
    }

}
