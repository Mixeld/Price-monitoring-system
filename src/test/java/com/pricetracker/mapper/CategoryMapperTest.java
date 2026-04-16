package com.pricetracker.mapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.pricetracker.dto.CategoryDto;
import com.pricetracker.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для CategoryMapper")
class CategoryMapperTest {

  private CategoryMapper categoryMapper;

  private Category existingCategory;
  private CategoryDto categoryDto;

  @BeforeEach
  void setUp() {
    categoryMapper = new CategoryMapper();

    existingCategory = new Category();
    existingCategory.setId(1L);
    existingCategory.setName("Electronics");

    categoryDto = new CategoryDto(1L, "Electronics");
  }

  // ==================== ТЕСТЫ ДЛЯ toDto ====================

  @Nested
  @DisplayName("Тесты метода toDto(Category)")
  class ToDtoTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация Category в CategoryDto")
    void toDto_shouldConvertCategoryToDto() {
      CategoryDto result = categoryMapper.toDto(existingCategory);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.name()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Category с null id")
    void toDto_shouldConvertCategoryWithNullId() {
      Category category = new Category();
      category.setId(null);
      category.setName("Books");

      CategoryDto result = categoryMapper.toDto(category);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.name()).isEqualTo("Books");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Category с null name")
    void toDto_shouldConvertCategoryWithNullName() {
      Category category = new Category();
      category.setId(2L);
      category.setName(null);

      CategoryDto result = categoryMapper.toDto(category);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(2L);
      assertThat(result.name()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Category с пустым name")
    void toDto_shouldConvertCategoryWithEmptyName() {
      Category category = new Category();
      category.setId(3L);
      category.setName("");

      CategoryDto result = categoryMapper.toDto(category);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(3L);
      assertThat(result.name()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Category с name из пробелов")
    void toDto_shouldConvertCategoryWithBlankName() {
      Category category = new Category();
      category.setId(4L);
      category.setName("   ");

      CategoryDto result = categoryMapper.toDto(category);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(4L);
      assertThat(result.name()).isEqualTo("   ");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null Category в null DTO")
    void toDto_shouldReturnNull_whenCategoryIsNull() {
      CategoryDto result = categoryMapper.toDto(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация Category со всеми null полями")
    void toDto_shouldHandleAllNullFields() {
      Category category = new Category();
      category.setId(null);
      category.setName(null);

      CategoryDto result = categoryMapper.toDto(category);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.name()).isNull();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ toEntity ====================

  @Nested
  @DisplayName("Тесты метода toEntity(CategoryDto)")
  class ToEntityTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация CategoryDto в Category")
    void toEntity_shouldConvertDtoToCategory() {
      Category result = categoryMapper.toEntity(categoryDto);

      assertThat(result).isNotNull();
      // ID не должен устанавливаться (это ответственность базы данных)
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация CategoryDto с null id")
    void toEntity_shouldConvertDtoWithNullId() {
      CategoryDto dto = new CategoryDto(null, "Books");

      Category result = categoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("Books");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация CategoryDto с null name")
    void toEntity_shouldConvertDtoWithNullName() {
      CategoryDto dto = new CategoryDto(1L, null);

      Category result = categoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация CategoryDto с пустым name")
    void toEntity_shouldConvertDtoWithEmptyName() {
      CategoryDto dto = new CategoryDto(1L, "");

      Category result = categoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация CategoryDto с name из пробелов")
    void toEntity_shouldConvertDtoWithBlankName() {
      CategoryDto dto = new CategoryDto(1L, "   ");

      Category result = categoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("   ");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация CategoryDto с id=0")
    void toEntity_shouldConvertDtoWithZeroId() {
      CategoryDto dto = new CategoryDto(0L, "Electronics");

      Category result = categoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull(); // ID не копируется
      assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null DTO (метод не имеет проверки на null!)")
    void toEntity_shouldThrowNPE_whenDtoIsNull() {
      // ВНИМАНИЕ: CategoryMapper.toEntity не проверяет dto на null
      // и выбросит NullPointerException при вызове dto.name()
      assertThatThrownBy(() -> categoryMapper.toEntity(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Интеграционные тесты (конвертация туда и обратно)")
  class RoundTripTests {

    @Test
    @DisplayName("[УСПЕХ] Round-trip конвертация: Category -> CategoryDto -> Category")
    void roundTrip_shouldPreserveData() {
      // Category -> CategoryDto
      CategoryDto dto = categoryMapper.toDto(existingCategory);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo(1L);
      assertThat(dto.name()).isEqualTo("Electronics");

      // CategoryDto -> Category
      Category categoryBack = categoryMapper.toEntity(dto);

      assertThat(categoryBack).isNotNull();
      assertThat(categoryBack.getId()).isNull(); // ID не восстанавливается
      assertThat(categoryBack.getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Создание нового Category через DTO")
    void roundTrip_shouldCreateNewCategory() {
      CategoryDto newCategoryDto = new CategoryDto(null, "New Category");

      Category entity = categoryMapper.toEntity(newCategoryDto);

      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isEqualTo("New Category");
    }
  }
}