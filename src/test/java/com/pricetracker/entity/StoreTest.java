package com.pricetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для Store entity (100% покрытие)")
class StoreTest {

  private final Long DEFAULT_ID = 1L;
  private final String DEFAULT_NAME = "Amazon";
  private final String DEFAULT_URL = "https://amazon.com";
  private final PriceHistory priceHistory1 = new PriceHistory(); // Простой мок-объект для тестов

  // ==================== ТЕСТЫ КОНСТРУКТОРОВ ====================

  @Nested
  @DisplayName("Тесты конструкторов")
  class ConstructorTests {

    @Test
    @DisplayName("Конструктор по умолчанию создает объект с null-полями и пустым списком")
    void noArgsConstructor_shouldCreateObjectWithNullsAndEmptyList() {
      Store store = new Store();

      assertThat(store.getId()).isNull();
      assertThat(store.getName()).isNull();
      assertThat(store.getWebsiteUrl()).isNull();
      assertThat(store.getPriceHistories()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Конструктор со всеми аргументами корректно устанавливает все поля")
    void allArgsConstructor_shouldSetAllFieldsCorrectly() {
      List<PriceHistory> histories = new ArrayList<>();
      Store store = new Store(DEFAULT_ID, DEFAULT_NAME, DEFAULT_URL, histories);

      assertThat(store.getId()).isEqualTo(DEFAULT_ID);
      assertThat(store.getName()).isEqualTo(DEFAULT_NAME);
      assertThat(store.getWebsiteUrl()).isEqualTo(DEFAULT_URL);
      assertThat(store.getPriceHistories()).isSameAs(histories);
    }
  }

  // ==================== ТЕСТЫ equals И hashCode (САМОЕ ВАЖНОЕ ДЛЯ ПОКРЫТИЯ) ====================

  @Nested
  @DisplayName("Тесты equals и hashCode для полного покрытия веток")
  class EqualsAndHashCodeCoverageTests {

    private Store store1;
    private Store store2;

    @BeforeEach
    void setUp() {
      // Перед каждым тестом создаем два полностью идентичных, но разных объекта
      store1 = new Store(DEFAULT_ID, DEFAULT_NAME, DEFAULT_URL, new ArrayList<>(List.of(priceHistory1)));
      store2 = new Store(DEFAULT_ID, DEFAULT_NAME, DEFAULT_URL, new ArrayList<>(List.of(priceHistory1)));
    }

    @Test
    @DisplayName("Контракт: equals true для идентичных объектов, hashCode одинаковый")
    void equals_shouldReturnTrue_forIdenticalObjects() {
      assertThat(store1).isEqualTo(store2);
      assertThat(store1.hashCode()).isEqualTo(store2.hashCode());
    }

    @Test
    @DisplayName("equals: true для одного и того же объекта")
    void equals_shouldReturnTrue_forSameObject() {
      assertThat(store1.equals(store1)).isTrue();
    }

    @Test
    @DisplayName("equals: false для null")
    void equals_shouldReturnFalse_forNull() {
      assertThat(store1.equals(null)).isFalse();
    }

    @Test
    @DisplayName("equals: false для объекта другого класса")
    void equals_shouldReturnFalse_forDifferentClass() {
      assertThat(store1.equals(new Object())).isFalse();
    }

    // --- Тесты на различия в полях (покрывают ветки `!field1.equals(field2)`) ---

    @Test
    @DisplayName("equals: false при разных id")
    void equals_shouldBeFalse_whenIdsDiffer() {
      store2.setId(2L);
      assertThat(store1).isNotEqualTo(store2);
    }

    @Test
    @DisplayName("equals: false при разных name")
    void equals_shouldBeFalse_whenNamesDiffer() {
      store2.setName("Ozon");
      assertThat(store1).isNotEqualTo(store2);
    }

    @Test
    @DisplayName("equals: false при разных websiteUrl")
    void equals_shouldBeFalse_whenUrlsDiffer() {
      store2.setWebsiteUrl("https://ozon.ru");
      assertThat(store1).isNotEqualTo(store2);
    }

    @Test
    @DisplayName("equals: false при разных priceHistories")
    void equals_shouldBeFalse_whenHistoriesDiffer() {
      store2.setPriceHistories(new ArrayList<>());
      assertThat(store1).isNotEqualTo(store2);
    }

    // --- Тесты на null-значения (покрывают ветки `field == null ? ...`) ---

    @Test
    @DisplayName("equals: false если у одного id = null")
    void equals_shouldBeFalse_whenOneIdIsNull() {
      store1.setId(null);
      assertThat(store1).isNotEqualTo(store2); // Покрывает ветку this.field == null
    }

    @Test
    @DisplayName("equals: false если у другого id = null")
    void equals_shouldBeFalse_whenOtherIdIsNull() {
      store2.setId(null);
      assertThat(store1).isNotEqualTo(store2); // Покрывает ветку other.field == null
    }

    @Test
    @DisplayName("equals: false если у одного name = null")
    void equals_shouldBeFalse_whenOneNameIsNull() {
      store1.setName(null);
      assertThat(store1).isNotEqualTo(store2);
    }

    @Test
    @DisplayName("equals: false если у другого name = null")
    void equals_shouldBeFalse_whenOtherNameIsNull() {
      store2.setName(null);
      assertThat(store1).isNotEqualTo(store2);
    }

    // ...Аналогично для остальных полей...

    @Test
    @DisplayName("equals: false если у одного websiteUrl = null")
    void equals_shouldBeFalse_whenOneUrlIsNull() {
      store1.setWebsiteUrl(null);
      assertThat(store1).isNotEqualTo(store2);
    }

    @Test
    @DisplayName("equals: false если у другого websiteUrl = null")
    void equals_shouldBeFalse_whenOtherUrlIsNull() {
      store2.setWebsiteUrl(null);
      assertThat(store1).isNotEqualTo(store2);
    }

    @Test
    @DisplayName("equals: false если у одного priceHistories = null")
    void equals_shouldBeFalse_whenOneHistoriesIsNull() {
      store1.setPriceHistories(null);
      assertThat(store1).isNotEqualTo(store2);
    }

    @Test
    @DisplayName("equals: false если у другого priceHistories = null")
    void equals_shouldBeFalse_whenOtherHistoriesIsNull() {
      store2.setPriceHistories(null);
      assertThat(store1).isNotEqualTo(store2);
    }

    @Test
    @DisplayName("hashCode: работает корректно с null-полями")
    void hashCode_shouldWorkWithNullFields() {
      Store nullStore = new Store();
      // Просто вызываем метод, чтобы убедиться, что он не падает и покрывается тестом
      assertThat(nullStore.hashCode()).isNotZero();
    }
  }

  // ==================== ТЕСТЫ toString ====================

  @Nested
  @DisplayName("Тесты toString для полного покрытия")
  class ToStringCoverageTests {

    @Test
    @DisplayName("toString: содержит все не-null поля")
    void toString_shouldContainAllNonNullFields() {
      Store store = new Store(DEFAULT_ID, DEFAULT_NAME, DEFAULT_URL, new ArrayList<>());
      String result = store.toString();

      assertThat(result).contains("id=" + DEFAULT_ID);
      assertThat(result).contains("name=" + DEFAULT_NAME);
      assertThat(result).contains("websiteUrl=" + DEFAULT_URL);
      assertThat(result).contains("priceHistories=[]");
    }

    @Test
    @DisplayName("toString: корректно отображает null-поля")
    void toString_shouldDisplayNullFieldsCorrectly() {
      Store nullStore = new Store(null, null, null, null);
      String result = nullStore.toString();

      assertThat(result).contains("id=null");
      assertThat(result).contains("name=null");
      assertThat(result).contains("websiteUrl=null");
      assertThat(result).contains("priceHistories=null");
    }
  }
  @Nested
  @DisplayName("Тесты для закрытия последних веток (100% покрытие)")
  class FinalCoverageGapTests {

    private Store store1;
    private Store store2;

    @BeforeEach
    void setUp() {
      // Создаем два полностью идентичных объекта
      store1 = new Store(DEFAULT_ID, DEFAULT_NAME, DEFAULT_URL, new ArrayList<>(List.of(priceHistory1)));
      store2 = new Store(DEFAULT_ID, DEFAULT_NAME, DEFAULT_URL, new ArrayList<>(List.of(priceHistory1)));
    }

    @Test
    @DisplayName("[EQUALS] Объекты равны, если у обоих id = null")
    void equals_shouldBeTrue_whenBothIdsAreNull() {
      store1.setId(null);
      store2.setId(null);
      assertThat(store1).isEqualTo(store2);
    }

    @Test
    @DisplayName("[EQUALS] Объекты равны, если у обоих name = null")
    void equals_shouldBeTrue_whenBothNamesAreNull() {
      store1.setName(null);
      store2.setName(null);
      assertThat(store1).isEqualTo(store2);
    }

    @Test
    @DisplayName("[EQUALS] Объекты равны, если у обоих websiteUrl = null")
    void equals_shouldBeTrue_whenBothUrlsAreNull() {
      store1.setWebsiteUrl(null);
      store2.setWebsiteUrl(null);
      assertThat(store1).isEqualTo(store2);
    }

    @Test
    @DisplayName("[EQUALS] Объекты равны, если у обоих priceHistories = null")
    void equals_shouldBeTrue_whenBothHistoriesAreNull() {
      store1.setPriceHistories(null);
      store2.setPriceHistories(null);
      assertThat(store1).isEqualTo(store2);
    }

    @Test
    @DisplayName("[HASHCODE] hashCode работает, если некоторые поля null, а некоторые нет")
    void hashCode_shouldWorkWithMixedNullAndNonNullFields() {
      // Этот тест гарантирует покрытие веток в hashCode для каждого поля
      Store storeWithMixedFields = new Store(DEFAULT_ID, null, DEFAULT_URL, null);

      // Просто вызываем, чтобы JaCoCo засчитал выполнение
      assertThat(storeWithMixedFields.hashCode()).isNotZero();
    }
  }

  @Nested
  @DisplayName("Тест на наследование для 100% покрытия equals")
  class InheritanceCoverageTest {

    // Вспомогательный дочерний класс, определенный прямо в тесте
    @lombok.EqualsAndHashCode(callSuper = true) // Важно! Включает поля родителя в equals/hashCode
    class SubStore extends Store {

      // Можно добавить доп. поле, но для теста это не обязательно
      private String extraField;

      public SubStore(Long id, String name, String websiteUrl, List<PriceHistory> priceHistories,
          String extraField) {
        super(id, name, websiteUrl, priceHistories);
        this.extraField = extraField;
      }
    }

    @Test
    @DisplayName("[EQUALS] Родительский класс не должен быть равен дочернему")
    void equals_shouldReturnFalse_whenComparingToSubclassInstance() {
      // Arrange
      Store parentStore = new Store(1L, "Amazon", "amazon.com", new ArrayList<>());
      SubStore childStore = new SubStore(1L, "Amazon", "amazon.com", new ArrayList<>(), "extra");

      // Act & Assert
      // Этот вызов пройдет по последней непокрытой ветке в методе Store.equals()
      assertThat(parentStore.equals(childStore)).isFalse();

      // Также проверим симметрию (хотя это покрывает equals в SubStore, а не в Store)
      assertThat(childStore.equals(parentStore)).isFalse();
    }
  }
}