package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.NewCategoryDto;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    List<CategoryDto> toCategoryDtoList(List<Category> categories);

    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCategoryByNew(NewCategoryDto newCategoryDto);
}
