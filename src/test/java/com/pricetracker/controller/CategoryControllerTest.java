package com.pricetracker.controller;

import com.pricetracker.dto.CategoryDto;
import com.pricetracker.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для CategoryController")
class CategoryControllerTest {

  @Mock
  private CategoryService categoryService;

  @InjectMocks
  private CategoryController categoryController;

  private CategoryDto categoryDto;
  private List<CategoryDto> categoryDtoList;

  @BeforeEach
  void setUp() {
    categoryDto = new CategoryDto(1L, "Electronics");
    categoryDtoList = List.of(categoryDto);
  }

  // ==================== ТЕСТЫ ДЛЯ getAllCategories ====================

  @Nested
  @DisplayName("Тесты метода getAllCategories()")
  class GetAllCategoriesTests {

    @Test
    @DisplayName("[УСПЕХ] Получение всех категорий")
    void getAllCategories_shouldReturnAllCategories() {
      when(categoryService.getAllCategories()).thenReturn(categoryDtoList);

      ResponseEntity<List<CategoryDto>> response = categoryController.getAllCategories();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      assertThat(response.getBody().get(0)).isEqualTo(categoryDto);
      verify(categoryService).getAllCategories();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка категорий")
    void getAllCategories_shouldReturnEmptyList_whenNoCategories() {
      when(categoryService.getAllCategories()).thenReturn(List.of());

      ResponseEntity<List<CategoryDto>> response = categoryController.getAllCategories();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
      verify(categoryService).getAllCategories();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение нескольких категорий")
    void getAllCategories_shouldReturnMultipleCategories() {
      CategoryDto category2 = new CategoryDto(2L, "Books");
      CategoryDto category3 = new CategoryDto(3L, "Clothing");
      List<CategoryDto> multipleCategories = List.of(categoryDto, category2, category3);

      when(categoryService.getAllCategories()).thenReturn(multipleCategories);

      ResponseEntity<List<CategoryDto>> response = categoryController.getAllCategories();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(3);
      assertThat(response.getBody()).containsExactly(categoryDto, category2, category3);
      verify(categoryService).getAllCategories();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getCategoryById ====================

  @Nested
  @DisplayName("Тесты метода getCategoryById(Long id)")
  class GetCategoryByIdTests {

    @Test
    @DisplayName("[УСПЕХ] Получение категории по ID")
    void getCategoryById_shouldReturnCategory_whenExists() {
      when(categoryService.getCategoryById(1L)).thenReturn(categoryDto);

      ResponseEntity<CategoryDto> response = categoryController.getCategoryById(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(categoryDto);
      verify(categoryService).getCategoryById(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение категории с ID = 0")
    void getCategoryById_shouldHandleZeroId() {
      CategoryDto zeroIdCategory = new CategoryDto(0L, "Zero Category");
      when(categoryService.getCategoryById(0L)).thenReturn(zeroIdCategory);

      ResponseEntity<CategoryDto> response = categoryController.getCategoryById(0L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().id()).isEqualTo(0L);
      verify(categoryService).getCategoryById(0L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение категории с большим ID")
    void getCategoryById_shouldHandleLargeId() {
      Long largeId = Long.MAX_VALUE;
      CategoryDto largeIdCategory = new CategoryDto(largeId, "Large ID Category");
      when(categoryService.getCategoryById(largeId)).thenReturn(largeIdCategory);

      ResponseEntity<CategoryDto> response = categoryController.getCategoryById(largeId);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().id()).isEqualTo(largeId);
      verify(categoryService).getCategoryById(largeId);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getCategoryByName ====================

  @Nested
  @DisplayName("Тесты метода getCategoryByName(String name)")
  class GetCategoryByNameTests {

    @Test
    @DisplayName("[УСПЕХ] Получение категории по имени")
    void getCategoryByName_shouldReturnCategory_whenExists() {
      when(categoryService.getCategoryByName("Electronics")).thenReturn(categoryDto);

      ResponseEntity<CategoryDto> response = categoryController.getCategoryByName("Electronics");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(categoryDto);
      verify(categoryService).getCategoryByName("Electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Получение категории с именем в нижнем регистре")
    void getCategoryByName_shouldHandleLowercaseName() {
      CategoryDto lowercaseCategory = new CategoryDto(1L, "electronics");
      when(categoryService.getCategoryByName("electronics")).thenReturn(lowercaseCategory);

      ResponseEntity<CategoryDto> response = categoryController.getCategoryByName("electronics");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo("electronics");
      verify(categoryService).getCategoryByName("electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Получение категории с именем в верхнем регистре")
    void getCategoryByName_shouldHandleUppercaseName() {
      CategoryDto uppercaseCategory = new CategoryDto(1L, "ELECTRONICS");
      when(categoryService.getCategoryByName("ELECTRONICS")).thenReturn(uppercaseCategory);

      ResponseEntity<CategoryDto> response = categoryController.getCategoryByName("ELECTRONICS");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo("ELECTRONICS");
      verify(categoryService).getCategoryByName("ELECTRONICS");
    }

    @Test
    @DisplayName("[УСПЕХ] Получение категории с именем содержащим пробелы")
    void getCategoryByName_shouldHandleNameWithSpaces() {
      String nameWithSpaces = "Consumer Electronics";
      CategoryDto spacedCategory = new CategoryDto(1L, nameWithSpaces);
      when(categoryService.getCategoryByName(nameWithSpaces)).thenReturn(spacedCategory);

      ResponseEntity<CategoryDto> response = categoryController.getCategoryByName(nameWithSpaces);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo(nameWithSpaces);
      verify(categoryService).getCategoryByName(nameWithSpaces);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение категории с пустым именем")
    void getCategoryByName_shouldHandleEmptyName() {
      CategoryDto emptyNameCategory = new CategoryDto(1L, "");
      when(categoryService.getCategoryByName("")).thenReturn(emptyNameCategory);

      ResponseEntity<CategoryDto> response = categoryController.getCategoryByName("");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEmpty();
      verify(categoryService).getCategoryByName("");
    }
  }

  // ==================== ТЕСТЫ ДЛЯ createCategory ====================

  @Nested
  @DisplayName("Тесты метода createCategory(CategoryDto categoryDto)")
  class CreateCategoryTests {

    @Test
    @DisplayName("[УСПЕХ] Создание категории")
    void createCategory_shouldCreateAndReturnCreatedCategory() {
      CategoryDto createdCategory = new CategoryDto(1L, "Electronics");
      when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(createdCategory);

      ResponseEntity<CategoryDto> response = categoryController.createCategory(categoryDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isEqualTo(createdCategory);
      assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/categories/1"));
      verify(categoryService).createCategory(categoryDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Создание категории с null id")
    void createCategory_shouldCreateCategoryWithNullId() {
      CategoryDto newCategory = new CategoryDto(null, "New Category");
      CategoryDto createdCategory = new CategoryDto(5L, "New Category");
      when(categoryService.createCategory(newCategory)).thenReturn(createdCategory);

      ResponseEntity<CategoryDto> response = categoryController.createCategory(newCategory);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().id()).isEqualTo(5L);
      assertThat(response.getBody().name()).isEqualTo("New Category");
      assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/categories/5"));
      verify(categoryService).createCategory(newCategory);
    }

    @Test
    @DisplayName("[УСПЕХ] Создание категории с пустым именем")
    void createCategory_shouldCreateCategoryWithEmptyName() {
      CategoryDto emptyNameCategory = new CategoryDto(null, "");
      CategoryDto createdCategory = new CategoryDto(2L, "");
      when(categoryService.createCategory(emptyNameCategory)).thenReturn(createdCategory);

      ResponseEntity<CategoryDto> response = categoryController.createCategory(emptyNameCategory);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().name()).isEmpty();
      verify(categoryService).createCategory(emptyNameCategory);
    }

    @Test
    @DisplayName("[УСПЕХ] Создание категории с именем из пробелов")
    void createCategory_shouldCreateCategoryWithBlankName() {
      CategoryDto blankNameCategory = new CategoryDto(null, "   ");
      CategoryDto createdCategory = new CategoryDto(3L, "   ");
      when(categoryService.createCategory(blankNameCategory)).thenReturn(createdCategory);

      ResponseEntity<CategoryDto> response = categoryController.createCategory(blankNameCategory);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().name()).isEqualTo("   ");
      verify(categoryService).createCategory(blankNameCategory);
    }

    @Test
    @DisplayName("[УСПЕХ] Создание категории с очень длинным именем")
    void createCategory_shouldCreateCategoryWithLongName() {
      String longName = "A".repeat(200);
      CategoryDto longNameCategory = new CategoryDto(null, longName);
      CategoryDto createdCategory = new CategoryDto(4L, longName);
      when(categoryService.createCategory(longNameCategory)).thenReturn(createdCategory);

      ResponseEntity<CategoryDto> response = categoryController.createCategory(longNameCategory);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().name()).hasSize(200);
      verify(categoryService).createCategory(longNameCategory);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ updateCategory ====================

  @Nested
  @DisplayName("Тесты метода updateCategory(Long id, CategoryDto categoryDto)")
  class UpdateCategoryTests {

    @Test
    @DisplayName("[УСПЕХ] Обновление категории")
    void updateCategory_shouldUpdateAndReturnUpdatedCategory() {
      CategoryDto updatedCategory = new CategoryDto(1L, "Updated Electronics");
      when(categoryService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(updatedCategory);

      ResponseEntity<CategoryDto> response = categoryController.updateCategory(1L, categoryDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(updatedCategory);
      verify(categoryService).updateCategory(1L, categoryDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление категории с новым именем")
    void updateCategory_shouldUpdateCategoryWithNewName() {
      CategoryDto updateDto = new CategoryDto(1L, "New Category Name");
      CategoryDto updatedCategory = new CategoryDto(1L, "New Category Name");
      when(categoryService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(updatedCategory);

      ResponseEntity<CategoryDto> response = categoryController.updateCategory(1L, updateDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo("New Category Name");
      verify(categoryService).updateCategory(1L, updateDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление категории с тем же именем")
    void updateCategory_shouldUpdateCategoryWithSameName() {
      when(categoryService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(categoryDto);

      ResponseEntity<CategoryDto> response = categoryController.updateCategory(1L, categoryDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo("Electronics");
      verify(categoryService).updateCategory(1L, categoryDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление категории с пустым именем")
    void updateCategory_shouldUpdateCategoryWithEmptyName() {
      CategoryDto emptyNameDto = new CategoryDto(1L, "");
      CategoryDto updatedCategory = new CategoryDto(1L, "");
      when(categoryService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(updatedCategory);

      ResponseEntity<CategoryDto> response = categoryController.updateCategory(1L, emptyNameDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEmpty();
      verify(categoryService).updateCategory(1L, emptyNameDto);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ deleteCategory ====================

  @Nested
  @DisplayName("Тесты метода deleteCategory(Long id)")
  class DeleteCategoryTests {

    @Test
    @DisplayName("[УСПЕХ] Удаление категории")
    void deleteCategory_shouldDeleteAndReturnNoContent() {
      doNothing().when(categoryService).deleteCategory(1L);

      ResponseEntity<Void> response = categoryController.deleteCategory(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(categoryService).deleteCategory(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление категории с ID = 0")
    void deleteCategory_shouldHandleZeroId() {
      doNothing().when(categoryService).deleteCategory(0L);

      ResponseEntity<Void> response = categoryController.deleteCategory(0L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(categoryService).deleteCategory(0L);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление категории с большим ID")
    void deleteCategory_shouldHandleLargeId() {
      Long largeId = Long.MAX_VALUE;
      doNothing().when(categoryService).deleteCategory(largeId);

      ResponseEntity<Void> response = categoryController.deleteCategory(largeId);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(categoryService).deleteCategory(largeId);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление категории с отрицательным ID")
    void deleteCategory_shouldHandleNegativeId() {
      Long negativeId = -1L;
      doNothing().when(categoryService).deleteCategory(negativeId);

      ResponseEntity<Void> response = categoryController.deleteCategory(negativeId);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(categoryService).deleteCategory(negativeId);
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Интеграционные тесты (цепочки вызовов)")
  class IntegrationTests {

    @Test
    @DisplayName("[ИНТЕГРАЦИЯ] Создание, получение, обновление и удаление категории")
    void fullCrudCycle_shouldWorkCorrectly() {
      // 1. Create
      CategoryDto newCategory = new CategoryDto(null, "Test Category");
      CategoryDto createdCategory = new CategoryDto(10L, "Test Category");
      when(categoryService.createCategory(newCategory)).thenReturn(createdCategory);

      ResponseEntity<CategoryDto> createResponse = categoryController.createCategory(newCategory);
      assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      Long createdId = createResponse.getBody().id();

      // 2. Get by ID
      when(categoryService.getCategoryById(createdId)).thenReturn(createdCategory);
      ResponseEntity<CategoryDto> getResponse = categoryController.getCategoryById(createdId);
      assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(getResponse.getBody().name()).isEqualTo("Test Category");

      // 3. Update
      CategoryDto updateDto = new CategoryDto(createdId, "Updated Category");
      CategoryDto updatedCategory = new CategoryDto(createdId, "Updated Category");
      when(categoryService.updateCategory(eq(createdId), any(CategoryDto.class))).thenReturn(updatedCategory);

      ResponseEntity<CategoryDto> updateResponse = categoryController.updateCategory(createdId, updateDto);
      assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(updateResponse.getBody().name()).isEqualTo("Updated Category");

      // 4. Delete
      doNothing().when(categoryService).deleteCategory(createdId);
      ResponseEntity<Void> deleteResponse = categoryController.deleteCategory(createdId);
      assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      verify(categoryService).createCategory(newCategory);
      verify(categoryService).getCategoryById(createdId);
      verify(categoryService).updateCategory(createdId, updateDto);
      verify(categoryService).deleteCategory(createdId);
    }

    @Test
    @DisplayName("[ИНТЕГРАЦИЯ] Получение всех категорий после создания")
    void getAllCategories_shouldIncludeNewlyCreatedCategory() {
      // Setup existing categories
      List<CategoryDto> existingCategories = List.of(
          new CategoryDto(1L, "Electronics"),
          new CategoryDto(2L, "Books")
      );
      when(categoryService.getAllCategories()).thenReturn(existingCategories);

      ResponseEntity<List<CategoryDto>> getBeforeCreate = categoryController.getAllCategories();
      assertThat(getBeforeCreate.getBody()).hasSize(2);

      // Create new category
      CategoryDto newCategory = new CategoryDto(null, "Clothing");
      CategoryDto createdCategory = new CategoryDto(3L, "Clothing");
      when(categoryService.createCategory(newCategory)).thenReturn(createdCategory);
      categoryController.createCategory(newCategory);

      // Get all after create
      List<CategoryDto> afterCreateCategories = List.of(
          new CategoryDto(1L, "Electronics"),
          new CategoryDto(2L, "Books"),
          new CategoryDto(3L, "Clothing")
      );
      when(categoryService.getAllCategories()).thenReturn(afterCreateCategories);

      ResponseEntity<List<CategoryDto>> getAfterCreate = categoryController.getAllCategories();
      assertThat(getAfterCreate.getBody()).hasSize(3);

      verify(categoryService, times(2)).getAllCategories();
      verify(categoryService).createCategory(newCategory);
    }
  }
}