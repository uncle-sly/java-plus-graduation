package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.NewCategoryDto;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.exception.EntityNotFoundException;
import ru.yandex.practicum.exception.InitiatorRequestException;
import ru.yandex.practicum.feignClient.EventClient;
import ru.yandex.practicum.mapper.CategoryMapper;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventClient eventClient;

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return categoryMapper.toCategoryDtoList(categories);
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Category.class, "Категория с ID - " + id + ", не найдена."));
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        return categoryMapper.toCategoryDto(
                categoryRepository.save(categoryMapper.toCategoryByNew(newCategoryDto))
        );
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Category.class, "Категория с ID - " + id + ", не найдена."));
        if (findEventsWithCategory(id)) {
            throw new InitiatorRequestException("По Категории с ID: " + category.getId() + " найдены события.");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Category.class, "Категория с ID - " + id + ", не найдена."));
        category.setName(categoryDto.getName());
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategorysList(List<Long> ids) {
        return categoryMapper.toCategoryDtoList(categoryRepository.findAllById(ids));
    }

    private Boolean findEventsWithCategory(Long id) {
        try {
            return eventClient.findEventsWithCategory(id);
        } catch (FeignException e) {
            throw new EntityNotFoundException(FeignException.class, "События с категорией ID - " + id + ", не найдены.");
        }
    }

}