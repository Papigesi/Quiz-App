package com.example.quizapp.quizappdemo.service;

import com.example.quizapp.quizappdemo.dto.CategoryDto;
import com.example.quizapp.quizappdemo.exception.BadRequestException;
import com.example.quizapp.quizappdemo.exception.ResourceNotFoundException;
import com.example.quizapp.quizappdemo.model.Category;
import com.example.quizapp.quizappdemo.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAll() {
        return categoryRepository.findAllByOrderByCategoryNameAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public CategoryDto create(CategoryDto dto) {
        String name = dto.getCategoryName().trim();
        if (categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            throw new BadRequestException("Bu isimde bir kategori zaten var: " + name);
        }
        Category category = new Category(name, trimOrNull(dto.getDescription()));
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = findOrThrow(id);
        String newName = dto.getCategoryName().trim();
        if (!category.getCategoryName().equalsIgnoreCase(newName)
                && categoryRepository.existsByCategoryNameIgnoreCase(newName)) {
            throw new BadRequestException("Bu isimde bir kategori zaten var: " + newName);
        }
        category.setCategoryName(newName);
        category.setDescription(trimOrNull(dto.getDescription()));
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = findOrThrow(id);
        int quizCount = category.getQuizzes() != null ? category.getQuizzes().size() : 0;
        if (quizCount > 0) {
            throw new BadRequestException(
                    "Bu kategoride " + quizCount + " quiz var. Önce o quizleri silmelisin.");
        }
        categoryRepository.delete(category);
    }

    public Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bu ID ile bir kategori bulunamadı: " + id));
    }

    private CategoryDto toDto(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setCategoryName(c.getCategoryName());
        dto.setDescription(c.getDescription());
        dto.setQuizCount(c.getQuizzes() != null ? c.getQuizzes().size() : 0);
        return dto;
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
