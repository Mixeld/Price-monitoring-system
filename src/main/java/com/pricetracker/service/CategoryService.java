package com.pricetracker.service;

import com.pricetracker.dto.CategoryDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.CategoryMapper;
import com.pricetracker.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String CATEGORY_NOT_FOUND_BY_ID = "Category not found with id: ";
    private static final String CATEGORY_NOT_FOUND_BY_NAME = "Category not found with name: ";
    private static final String CATEGORY_ALREADY_EXISTS = "Category with name '%s' already exists";
    private static final String CANNOT_DELETE_CATEGORY_WITH_PRODUCTS = 
            "Cannot delete category '%s' because it has %d associated products. " +
            "Remove or reassign these products first.";

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        log.debug("Getting all categories");
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        log.debug("Getting category by id: {}", id);
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + id));
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryByName(String name) {
        log.debug("Getting category by name: {}", name);
        return categoryRepository.findByName(name)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_NAME + name));
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        log.debug("Creating new category with name: {}", dto.name());
        
        if (categoryRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format(CATEGORY_ALREADY_EXISTS, dto.name()));
        }

        Category category = categoryMapper.toEntity(dto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {} and name: {}",
                savedCategory.getId(), savedCategory.getName());

        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        log.debug("Updating category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + id));

        if (!category.getName().equals(dto.name()) &&
                categoryRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException(
                    String.format(CATEGORY_ALREADY_EXISTS, dto.name()));
        }

        category.setName(dto.name());
        log.info("Category updated successfully with id: {} -> new name: {}", id, dto.name());

        return categoryMapper.toDto(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.debug("Deleting category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + id));

        if (!category.getProducts().isEmpty()) {
            int productCount = category.getProducts().size();
            log.warn("Cannot delete category with id: {} because it has {} associated products",
                    id, productCount);
            throw new IllegalStateException(
                    String.format(CANNOT_DELETE_CATEGORY_WITH_PRODUCTS, 
                            category.getName(), productCount));
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {} and name: {}", id, category.getName());
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.findByName(name).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Long> getProductIdsInCategory(Long categoryId) {
        log.debug("Getting product IDs for category: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + categoryId));

        return category.getProducts().stream()
                .map(Product::getId)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getProductCount(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND_BY_ID + categoryId));
        
        return category.getProducts().size();
    }
}
