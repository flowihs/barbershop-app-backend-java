package com.github.barbershop.category.service;

import com.github.barbershop.category.dto.CategoryResponse;
import com.github.barbershop.category.entity.Category;
import com.github.barbershop.category.exception.CategoryNotFoundException;
import com.github.barbershop.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAll() {
        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            throw new CategoryNotFoundException();
        }

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}