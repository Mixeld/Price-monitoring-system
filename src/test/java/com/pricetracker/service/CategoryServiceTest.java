package com.pricetracker.service;

import com.pricetracker.dto.CategoryDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.mapper.CategoryMapper;
import com.pricetracker.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для CategoryService")
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;
  @Mock
  private CategoryMapper categoryMapper;

  @InjectMocks
  private CategoryService categoryService;

  private Category existingCategory;
  private CategoryDto categoryDto;

  @BeforeEach
  void setUp() {
    existingCategory = new Category();
    existingCategory.setId(1L);
    existingCategory.setName("Electronics");
    existingCategory.setProducts(Collections.emptyList());

    categoryDto = new CategoryDto(1L, "Electronics");
  }

  @Nested
  @DisplayName("Тесты на чтение (Read)")
  class ReadTests {
    @Test
    @DisplayName("getAllCategories должен возвращать список DTO")
    void getAllCategories_shouldReturnDtoList() {
      when(categoryRepository.findAll()).thenReturn(List.of(existingCategory));
      when(categoryMapper.toDto(existingCategory)).thenReturn(categoryDto);

      List<CategoryDto> result = categoryService.getAllCategories();

      assertThat(result).hasSize(1).contains(categoryDto);
    }

    @Test
    @DisplayName("getCategoryById должен возвращать DTO, если категория найдена")
    void getCategoryById_shouldReturnDto_whenFound() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
      when(categoryMapper.toDto(existingCategory)).thenReturn(categoryDto);

      CategoryDto result = categoryService.getCategoryById(1L);

      assertThat(result).isEqualTo(categoryDto);
    }

    @Test
    @DisplayName("getCategoryById должен бросать исключение, если категория не найдена")
    void getCategoryById_shouldThrowException_whenNotFound() {
      when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> categoryService.getCategoryById(99L))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getCategoryByName должен возвращать DTO, если категория найдена")
    void getCategoryByName_shouldReturnDto_whenFound() {
      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(existingCategory));
      when(categoryMapper.toDto(existingCategory)).thenReturn(categoryDto);

      CategoryDto result = categoryService.getCategoryByName("Electronics");

      assertThat(result).isEqualTo(categoryDto);
    }

    @Test
    @DisplayName("getCategoryByName должен бросать исключение, если категория не найдена")
    void getCategoryByName_shouldThrowException_whenNotFound() {
      when(categoryRepository.findByName("Unknown")).thenReturn(Optional.empty());
      assertThatThrownBy(() -> categoryService.getCategoryByName("Unknown"))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("Тесты на создание (Create)")
  class CreateTests {
    @Test
    @DisplayName("createCategory должен успешно создавать категорию")
    void createCategory_shouldSucceed_whenNameIsUnique() {
      CategoryDto createDto = new CategoryDto(null, "Books");
      when(categoryRepository.findByName("Books")).thenReturn(Optional.empty());
      when(categoryMapper.toEntity(any())).thenReturn(new Category());
      when(categoryRepository.save(any())).thenReturn(new Category());
      when(categoryMapper.toDto(any())).thenReturn(new CategoryDto(2L, "Books"));

      CategoryDto result = categoryService.createCategory(createDto);

      assertThat(result.id()).isEqualTo(2L);
      verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory должен бросать исключение, если имя уже существует")
    void createCategory_shouldThrowException_whenNameExists() {
      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(existingCategory));
      assertThatThrownBy(() -> categoryService.createCategory(new CategoryDto(null, "Electronics")))
          .isInstanceOf(DuplicateResourceException.class);
    }
  }

  @Nested
  @DisplayName("Тесты на обновление (Update)")
  class UpdateTests {
    @Test
    @DisplayName("updateCategory должен успешно обновлять имя")
    void updateCategory_shouldSucceed_withNewName() {
      CategoryDto updateDto = new CategoryDto(1L, "New Electronics");
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
      when(categoryRepository.findByName("New Electronics")).thenReturn(Optional.empty());
      when(categoryMapper.toDto(any())).thenReturn(new CategoryDto(1L, "New Electronics"));

      CategoryDto result = categoryService.updateCategory(1L, updateDto);

      assertThat(result.name()).isEqualTo("New Electronics");
      assertThat(existingCategory.getName()).isEqualTo("New Electronics");
    }

    @Test
    @DisplayName("updateCategory должен успешно завершаться, если имя не меняется")
    void updateCategory_shouldSucceed_whenNameIsUnchanged() {
      CategoryDto updateDto = new CategoryDto(1L, "Electronics");
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
      when(categoryMapper.toDto(any())).thenReturn(categoryDto);

      categoryService.updateCategory(1L, updateDto);

      // Проверяем, что поиск по имени не вызывался, т.к. имя не менялось
      verify(categoryRepository, never()).findByName("Electronics");
    }

    @Test
    @DisplayName("updateCategory должен бросать исключение, если категория не найдена")
    void updateCategory_shouldThrowException_whenNotFound() {
      when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> categoryService.updateCategory(99L, categoryDto))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("updateCategory должен бросать исключение, если имя занято другой категорией")
    void updateCategory_shouldThrowException_whenNameIsTaken() {
      CategoryDto updateDto = new CategoryDto(1L, "Computers");
      Category otherCategory = new Category();
      otherCategory.setId(2L);
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
      when(categoryRepository.findByName("Computers")).thenReturn(Optional.of(otherCategory));
      assertThatThrownBy(() -> categoryService.updateCategory(1L, updateDto))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("Тесты на удаление (Delete)")
  class DeleteTests {
    @Test
    @DisplayName("deleteCategory должен успешно удалять пустую категорию")
    void deleteCategory_shouldSucceed_whenCategoryIsEmpty() {
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
      doNothing().when(categoryRepository).delete(existingCategory);
      categoryService.deleteCategory(1L);
      verify(categoryRepository).delete(existingCategory);
    }

    @Test
    @DisplayName("deleteCategory должен бросать исключение, если у категории есть продукты")
    void deleteCategory_shouldThrowException_whenCategoryHasProducts() {
      existingCategory.setProducts(List.of(new Product()));
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
      assertThatThrownBy(() -> categoryService.deleteCategory(1L))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("deleteCategory должен бросать исключение, если категория не найдена")
    void deleteCategory_shouldThrowException_whenNotFound() {
      when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> categoryService.deleteCategory(99L))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("Тесты на вспомогательные методы")
  class UtilMethodsTests {
    @Test
    @DisplayName("existsByName должен возвращать true/false")
    void existsByName_shouldReturnCorrectBoolean() {
      when(categoryRepository.findByName("Existing")).thenReturn(Optional.of(existingCategory));
      when(categoryRepository.findByName("NonExisting")).thenReturn(Optional.empty());

      assertThat(categoryService.existsByName("Existing")).isTrue();
      assertThat(categoryService.existsByName("NonExisting")).isFalse();
    }

    @Test
    @DisplayName("getProductIdsInCategory должен возвращать список ID")
    void getProductIdsInCategory_shouldReturnIdList() {
      Product p1 = new Product(); p1.setId(101L);
      Product p2 = new Product(); p2.setId(102L);
      existingCategory.setProducts(List.of(p1, p2));
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));

      List<Long> result = categoryService.getProductIdsInCategory(1L);

      assertThat(result).hasSize(2).containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    @DisplayName("getProductIdsInCategory должен бросать исключение, если категория не найдена")
    void getProductIdsInCategory_shouldThrowException_whenNotFound() {
      when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> categoryService.getProductIdsInCategory(99L))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getProductCount должен возвращать количество продуктов")
    void getProductCount_shouldReturnProductCount() {
      existingCategory.setProducts(List.of(new Product(), new Product()));
      when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));

      long count = categoryService.getProductCount(1L);

      assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("getProductCount должен бросать исключение, если категория не найдена")
    void getProductCount_shouldThrowException_whenNotFound() {
      when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> categoryService.getProductCount(99L))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }
}