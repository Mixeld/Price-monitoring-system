package com.pricetracker.service.base;

import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

  @Mock
  private JpaRepository<TestEntity, Long> repository;

  private ConcreteBaseService service;

  @BeforeEach
  void setUp() {
    service = new ConcreteBaseService(repository);
  }

  @Test
  void getAll_ShouldReturnAllDtos() {
    TestEntity entity1 = new TestEntity(1L, "Entity1", "Value1");
    TestEntity entity2 = new TestEntity(2L, "Entity2", "Value2");
    when(repository.findAll()).thenReturn(List.of(entity1, entity2));

    List<TestDto> result = service.getAll();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getName()).isEqualTo("Entity1");
    verify(repository).findAll();
  }

  @Test
  void getById_WhenEntityExists_ShouldReturnDto() {
    TestEntity entity = new TestEntity(1L, "Entity1", "Value1");
    when(repository.findById(1L)).thenReturn(Optional.of(entity));

    TestDto result = service.getById(1L);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(repository).findById(1L);
  }

  @Test
  void getById_WhenEntityNotFound_ShouldThrowException() {
    when(repository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(999L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void create_ShouldSaveAndReturnDto() {
    TestDto input = new TestDto(null, "NewEntity", "NewValue");
    TestEntity savedEntity = new TestEntity(1L, "NewEntity", "NewValue");
    when(repository.save(any(TestEntity.class))).thenReturn(savedEntity);

    TestDto result = service.create(input);

    assertThat(result.getId()).isEqualTo(1L);
    verify(repository).save(any(TestEntity.class));
  }

  @Test
  void update_WhenEntityExists_ShouldUpdate() {
    TestEntity existing = new TestEntity(1L, "OldName", "OldValue");
    TestDto updateDto = new TestDto(1L, "NewName", "NewValue");

    when(repository.findById(1L)).thenReturn(Optional.of(existing));
    when(repository.save(any(TestEntity.class))).thenAnswer(invocation -> {
      TestEntity entity = invocation.getArgument(0);
      return entity;
    });

    TestDto result = service.update(1L, updateDto);

    assertThat(result.getName()).isEqualTo("NewName");
    assertThat(result.getValue()).isEqualTo("NewValue");
    verify(repository).findById(1L);
    verify(repository).save(any(TestEntity.class));
  }

  @Test
  void update_WhenEntityNotFound_ShouldThrowException() {
    TestDto updateDto = new TestDto(999L, "Name", "Value");
    when(repository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(999L, updateDto))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void delete_WhenEntityExists_ShouldDelete() {
    TestEntity existing = new TestEntity(1L, "ToDelete", "Value");
    when(repository.findById(1L)).thenReturn(Optional.of(existing));

    service.delete(1L);

    verify(repository).delete(existing);
  }

  @Test
  void delete_WhenEntityNotFound_ShouldThrowException() {
    when(repository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(999L))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(repository, never()).delete(any());
  }

  @Test
  void existsById_ShouldReturnCorrectValue() {
    when(repository.existsById(1L)).thenReturn(true);
    assertThat(service.existsById(1L)).isTrue();

    when(repository.existsById(999L)).thenReturn(false);
    assertThat(service.existsById(999L)).isFalse();
  }

  @Test
  void checkUnique_WhenTrue_ShouldThrowException() {
    assertThatThrownBy(() -> service.checkUnique(() -> true, "email", "test@test.com"))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void checkUnique_WhenFalse_ShouldNotThrowException() {
    service.checkUnique(() -> false, "email", "test@test.com");
  }
}

// Обычные классы с getter/setter
class TestEntity {
  private Long id;
  private String name;
  private String value;

  public TestEntity() {}

  public TestEntity(Long id, String name, String value) {
    this.id = id;
    this.name = name;
    this.value = value;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getValue() { return value; }
  public void setValue(String value) { this.value = value; }
}

class TestDto {
  private Long id;
  private String name;
  private String value;

  public TestDto() {}

  public TestDto(Long id, String name, String value) {
    this.id = id;
    this.name = name;
    this.value = value;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getValue() { return value; }
  public void setValue(String value) { this.value = value; }
}

// Конкретная реализация BaseService
class ConcreteBaseService extends BaseService<TestEntity, TestDto, Long> {

  private boolean beforeSaveCalled = false;
  private boolean afterSaveCalled = false;
  private boolean beforeUpdateCalled = false;
  private boolean beforeDeleteCalled = false;
  private boolean afterDeleteCalled = false;
  private boolean validateBeforeCreateCalled = false;
  private boolean validateBeforeUpdateCalled = false;
  private boolean validateBeforeDeleteCalled = false;

  public ConcreteBaseService(JpaRepository<TestEntity, Long> repository) {
    super(repository, "TestEntity",
        entity -> new TestDto(entity.getId(), entity.getName(), entity.getValue()),
        dto -> new TestEntity(dto.getId(), dto.getName(), dto.getValue()));
  }

  @Override
  protected Long getIdValue(TestEntity entity) {
    return entity.getId();
  }

  public boolean isBeforeSaveCalled() { return beforeSaveCalled; }
  public boolean isAfterSaveCalled() { return afterSaveCalled; }
  public boolean isBeforeUpdateCalled() { return beforeUpdateCalled; }
  public boolean isBeforeDeleteCalled() { return beforeDeleteCalled; }
  public boolean isAfterDeleteCalled() { return afterDeleteCalled; }
  public boolean isValidateBeforeCreateCalled() { return validateBeforeCreateCalled; }
  public boolean isValidateBeforeUpdateCalled() { return validateBeforeUpdateCalled; }
  public boolean isValidateBeforeDeleteCalled() { return validateBeforeDeleteCalled; }

  @Override
  protected void beforeSave(TestEntity entity) {
    beforeSaveCalled = true;
    super.beforeSave(entity);
  }

  @Override
  protected void afterSave(TestEntity entity) {
    afterSaveCalled = true;
    super.afterSave(entity);
  }

  @Override
  protected void beforeUpdate(TestEntity entity) {
    beforeUpdateCalled = true;
    super.beforeUpdate(entity);
  }

  @Override
  protected void beforeDelete(TestEntity entity) {
    beforeDeleteCalled = true;
    super.beforeDelete(entity);
  }

  @Override
  protected void afterDelete() {
    afterDeleteCalled = true;
    super.afterDelete();
  }

  @Override
  protected void validateBeforeCreate(TestDto dto) {
    validateBeforeCreateCalled = true;
    super.validateBeforeCreate(dto);
  }

  @Override
  protected void validateBeforeUpdate(Long id, TestDto dto, TestEntity entity) {
    validateBeforeUpdateCalled = true;
    super.validateBeforeUpdate(id, dto, entity);
  }

  @Override
  protected void validateBeforeDelete(TestEntity entity) {
    validateBeforeDeleteCalled = true;
    super.validateBeforeDelete(entity);
  }

  @Override
  protected void updateEntity(TestEntity entity, TestDto dto) {
    entity.setName(dto.getName());
    entity.setValue(dto.getValue());
  }
}