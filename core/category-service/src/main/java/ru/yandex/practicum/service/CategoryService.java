package ru.yandex.practicum.service;


import ru.yandex.practicum.dto.NewCategoryDto;
import ru.yandex.practicum.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getCategoryById(Long id);

    List<CategoryDto> getCategorysList(List<Long> ids);

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

}