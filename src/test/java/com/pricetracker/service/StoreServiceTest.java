package com.pricetracker.service;

import com.pricetracker.dto.StoreDto;
import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Store;
import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.mapper.StoreMapper;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.StoreRepository;
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
@DisplayName("Unit-тесты для StoreService")
class StoreServiceTest {

  @Mock
  private StoreRepository storeRepository;
  @Mock
  private PriceHistoryRepository priceHistoryRepository;
  @Mock
  private StoreMapper storeMapper;

  @InjectMocks
  private StoreService storeService;

  private Store existingStore;
  private StoreDto storeDto;

  @BeforeEach
  void setUp() {
    existingStore = new Store(1L, "Amazon", "https://amazon.com", Collections.emptyList());
    storeDto = new StoreDto(1L, "Amazon", "https://amazon.com");
  }

  @Nested
  @DisplayName("Тесты на создание (Create)")
  class CreateTests {


    @Test
    @DisplayName("Успешное создание магазина с уникальными данными")
    void createStore_shouldSucceed_withUniqueData() {
      StoreDto createDto = new StoreDto(null, "New Store", "https://newstore.com");
      Store newStore = new Store(null, "New Store", "https://newstore.com", null);
      when(storeRepository.findByName("New Store")).thenReturn(Optional.empty());
      when(storeRepository.findByWebsiteUrl("https://newstore.com")).thenReturn(Optional.empty());
      when(storeMapper.toEntity(createDto)).thenReturn(newStore);
      when(storeRepository.save(newStore)).thenReturn(new Store(2L, "New Store", "https://newstore.com", null));
      when(storeMapper.toDto(any(Store.class))).thenReturn(new StoreDto(2L, "New Store", "https://newstore.com"));

      StoreDto result = storeService.createStore(createDto);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(2L);
      verify(storeRepository).save(newStore);
    }

    @Test
    @DisplayName("Успешное создание магазина с пустым (но не null) URL")
    void createStore_shouldSucceed_withEmptyUrl() {
      StoreDto createDto = new StoreDto(null, "New Store", "");
      when(storeRepository.findByName("New Store")).thenReturn(Optional.empty());
      when(storeMapper.toEntity(any())).thenReturn(new Store());
      when(storeRepository.save(any())).thenReturn(new Store());
      when(storeMapper.toDto(any())).thenReturn(new StoreDto(1L, "New Store", ""));

      storeService.createStore(createDto);

      // Проверяем, что проверка URL не вызывалась для пустой строки
      verify(storeRepository, never()).findByWebsiteUrl(anyString());
    }

    // --- НОВЫЙ ТЕСТ для покрытия ветки с null URL ---
    @Test
    @DisplayName("Успешное создание магазина с null URL")
    void createStore_shouldSucceed_withNullUrl() {
      StoreDto createDto = new StoreDto(null, "New Store", null);
      when(storeRepository.findByName("New Store")).thenReturn(Optional.empty());
      when(storeMapper.toEntity(any())).thenReturn(new Store());
      when(storeRepository.save(any())).thenReturn(new Store());
      when(storeMapper.toDto(any())).thenReturn(new StoreDto(1L, "New Store", null));

      storeService.createStore(createDto);

      verify(storeRepository, never()).findByWebsiteUrl(any());
    }

    @Test
    @DisplayName("Ошибка при создании магазина с существующим именем")
    void createStore_shouldFail_whenNameExists() {
      StoreDto createDto = new StoreDto(null, "Amazon", "https://newurl.com");
      when(storeRepository.findByName("Amazon")).thenReturn(Optional.of(existingStore));

      assertThatThrownBy(() -> storeService.createStore(createDto))
          .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Ошибка при создании магазина с существующим URL")
    void createStore_shouldFail_whenUrlExists() {
      StoreDto createDto = new StoreDto(null, "New Store", "https://amazon.com");
      when(storeRepository.findByName("New Store")).thenReturn(Optional.empty());
      when(storeRepository.findByWebsiteUrl("https://amazon.com")).thenReturn(Optional.of(existingStore));

      assertThatThrownBy(() -> storeService.createStore(createDto))
          .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Успешное создание магазина с URL из пробелов")
    void createStore_shouldSucceed_withWhitespaceUrl() {
      // Arrange
      StoreDto createDto = new StoreDto(null, "New Store", "   "); // URL из пробелов
      when(storeRepository.findByName("New Store")).thenReturn(Optional.empty());
      // Мокируем для успешного выполнения
      when(storeMapper.toEntity(any())).thenReturn(new Store());
      when(storeRepository.save(any())).thenReturn(new Store());
      when(storeMapper.toDto(any())).thenReturn(new StoreDto(1L, "New Store", "   "));

      // Act
      storeService.createStore(createDto);

      // Assert
      // Проверяем, что из-за isBlank() вызов к репозиторию для проверки URL НЕ ПРОИЗОШЕЛ
      verify(storeRepository, never()).findByWebsiteUrl(anyString());
    }
  }

  @Nested
  @DisplayName("Тесты на чтение (Read)")
  class ReadTests {
    @Test
    @DisplayName("Получение всех магазинов")
    void getAllStores_shouldReturnDtoList() {
      when(storeRepository.findAll()).thenReturn(List.of(existingStore));
      when(storeMapper.toDto(existingStore)).thenReturn(storeDto);
      List<StoreDto> result = storeService.getAllStores();
      assertThat(result).hasSize(1).contains(storeDto);
    }

    @Test
    @DisplayName("Получение магазина по ID, если он существует")
    void getStoreById_shouldReturnDto_whenFound() {
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(storeMapper.toDto(existingStore)).thenReturn(storeDto);
      StoreDto result = storeService.getStoreById(1L);
      assertThat(result).isEqualTo(storeDto);
    }

    @Test
    @DisplayName("Ошибка при поиске по ID, если магазин не найден")
    void getStoreById_shouldFail_whenNotFound() {
      when(storeRepository.findById(99L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> storeService.getStoreById(99L))
          .isInstanceOf(EntityNotFoundException.class);
    }

    // --- НОВЫЙ ТЕСТ для getStoreByName ---
    @Test
    @DisplayName("Получение магазина по имени, если он существует")
    void getStoreByName_shouldReturnDto_whenFound() {
      when(storeRepository.findByName("Amazon")).thenReturn(Optional.of(existingStore));
      when(storeMapper.toDto(existingStore)).thenReturn(storeDto);
      StoreDto result = storeService.getStoreByName("Amazon");
      assertThat(result).isEqualTo(storeDto);
    }

    // --- НОВЫЙ ТЕСТ для getStoreByName (негативный) ---
    @Test
    @DisplayName("Ошибка при поиске по имени, если магазин не найден")
    void getStoreByName_shouldFail_whenNotFound() {
      when(storeRepository.findByName("Unknown")).thenReturn(Optional.empty());
      assertThatThrownBy(() -> storeService.getStoreByName("Unknown"))
          .isInstanceOf(EntityNotFoundException.class);
    }

    // --- НОВЫЙ ТЕСТ для searchStoresByName ---
    @Test
    @DisplayName("Поиск магазинов по части имени")
    void searchStoresByName_shouldReturnMatchingStores() {
      when(storeRepository.findByNameContainingIgnoreCase("zon")).thenReturn(List.of(existingStore));
      when(storeMapper.toDto(existingStore)).thenReturn(storeDto);
      List<StoreDto> result = storeService.searchStoresByName("zon");
      assertThat(result).hasSize(1).contains(storeDto);
    }
  }

  @Nested
  @DisplayName("Тесты на обновление (Update)")
  class UpdateTests {
    @Test
    @DisplayName("Успешное обновление магазина")
    void updateStore_shouldSucceed() {
      StoreDto updateDto = new StoreDto(1L, "Amazon Prime", "https://prime.amazon.com");
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(storeMapper.toDto(existingStore)).thenReturn(updateDto);
      StoreDto result = storeService.updateStore(1L, updateDto);
      assertThat(result.name()).isEqualTo("Amazon Prime");
    }

    // --- НОВЫЙ ТЕСТ для покрытия ветки с null URL ---
    @Test
    @DisplayName("Успешное обновление магазина с null URL")
    void updateStore_shouldSucceed_withNullUrl() {
      StoreDto updateDto = new StoreDto(1L, "Amazon Prime", null);
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(storeMapper.toDto(any())).thenReturn(updateDto);
      storeService.updateStore(1L, updateDto);
      assertThat(existingStore.getWebsiteUrl()).isNull();
      verify(storeRepository, never()).findByWebsiteUrl(any());
    }

    // Внутри @Nested class UpdateTests { ... }

    @Test
    @DisplayName("Ошибка при обновлении несуществующего магазина")
    void updateStore_shouldFail_whenStoreNotFound() {
      when(storeRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> storeService.updateStore(99L, storeDto))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Успешное обновление имени, когда URL не меняется")
    void updateStore_shouldSucceed_whenUrlIsUnchanged() {
      // Arrange: DTO с новым именем, но старым URL
      StoreDto updateDto = new StoreDto(1L, "New Amazon", "https://amazon.com");
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(storeRepository.findByName("New Amazon")).thenReturn(Optional.empty());
      when(storeMapper.toDto(any())).thenReturn(updateDto);

      // Act
      storeService.updateStore(1L, updateDto);

      // Assert: Проверяем, что имя обновилось
      assertThat(existingStore.getName()).isEqualTo("New Amazon");
      // и что проверка на дубликат URL не вызывалась, т.к. URL не менялся
      verify(storeRepository, never()).findByWebsiteUrl(anyString());
    }

    @Test
    @DisplayName("Ошибка при обновлении на занятое имя")
    void updateStore_shouldFail_whenNameIsTaken() {
      StoreDto updateDto = new StoreDto(1L, "Another Store", "https://amazon.com");
      Store anotherStore = new Store(2L, "Another Store", "https://another.com", null);
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(storeRepository.findByName("Another Store")).thenReturn(Optional.of(anotherStore));
      assertThatThrownBy(() -> storeService.updateStore(1L, updateDto))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Ошибка при обновлении на занятый URL")
    void updateStore_shouldFail_whenUrlIsTaken() {
      StoreDto updateDto = new StoreDto(1L, "Amazon", "https://another.com");
      Store anotherStore = new Store(2L, "Another Store", "https://another.com", null);
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(storeRepository.findByWebsiteUrl("https://another.com")).thenReturn(Optional.of(anotherStore));
      assertThatThrownBy(() -> storeService.updateStore(1L, updateDto))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Успешное обновление, когда URL меняется на пустую строку")
    void updateStore_shouldSucceed_withBlankUrl() {
      // Arrange
      StoreDto updateDto = new StoreDto(1L, "New Name", "   "); // URL из пробелов
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(storeRepository.findByName("New Name")).thenReturn(Optional.empty());
      when(storeMapper.toDto(any())).thenReturn(updateDto);

      // Act
      storeService.updateStore(1L, updateDto);

      // Assert
      assertThat(existingStore.getName()).isEqualTo("New Name");
      assertThat(existingStore.getWebsiteUrl()).isEqualTo("   ");
      verify(storeRepository, never()).findByWebsiteUrl(anyString());
    }
  }

  @Nested
  @DisplayName("Тесты на удаление (Delete)")
  class DeleteTests {
    @Test
    @DisplayName("Успешное удаление магазина без истории цен")
    void deleteStore_shouldSucceed_whenNoHistory() {
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(1L)).thenReturn(Collections.emptyList());
      storeService.deleteStore(1L);
      verify(storeRepository).delete(existingStore);
    }

    @Test
    @DisplayName("Ошибка при удалении магазина с историей цен")
    void deleteStore_shouldFail_whenHistoryExists() {
      when(storeRepository.findById(1L)).thenReturn(Optional.of(existingStore));
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(1L)).thenReturn(List.of(new PriceHistory()));
      assertThatThrownBy(() -> storeService.deleteStore(1L))
          .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Ошибка при удалении несуществующего магазина")
    void deleteStore_shouldFail_whenStoreNotFound() {
      when(storeRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> storeService.deleteStore(99L))
          .isInstanceOf(EntityNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("Тесты на вспомогательные методы")
  class UtilMethodsTests {
    @Test
    @DisplayName("getPriceHistoryCount возвращает правильное количество")
    void getPriceHistoryCount_shouldReturnCorrectCount() {
      when(storeRepository.existsById(1L)).thenReturn(true);
      when(priceHistoryRepository.countByStoreId(1L)).thenReturn(5L);
      long count = storeService.getPriceHistoryCount(1L);
      assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("getPriceHistoryCount бросает исключение, если магазин не найден")
    void getPriceHistoryCount_shouldFail_whenStoreNotFound() {
      when(storeRepository.existsById(99L)).thenReturn(false);
      assertThatThrownBy(() -> storeService.getPriceHistoryCount(99L))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("existsByWebsiteUrl возвращает false для null или пустого URL")
    void existsByWebsiteUrl_shouldReturnFalse_forBlankUrl() {
      assertThat(storeService.existsByWebsiteUrl(null)).isFalse();
      assertThat(storeService.existsByWebsiteUrl("  ")).isFalse();
      verify(storeRepository, never()).findByWebsiteUrl(any());
    }

    // --- НОВЫЙ ТЕСТ для existsByName и existsByWebsiteUrl (позитивные) ---
    @Test
    @DisplayName("existsByName и existsByWebsiteUrl возвращают true, когда запись найдена")
    void existsMethods_shouldReturnTrue_whenFound() {
      when(storeRepository.findByName("Amazon")).thenReturn(Optional.of(existingStore));
      when(storeRepository.findByWebsiteUrl("https://amazon.com")).thenReturn(Optional.of(existingStore));

      assertThat(storeService.existsByName("Amazon")).isTrue();
      assertThat(storeService.existsByWebsiteUrl("https://amazon.com")).isTrue();
    }
  }
}