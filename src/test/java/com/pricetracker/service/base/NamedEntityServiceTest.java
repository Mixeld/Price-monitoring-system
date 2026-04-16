package com.pricetracker.service.base;

import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamedEntityServiceTest {

  @Mock
  private JpaRepository<TestEntity, Long> repository;

  private ConcreteNamedEntityService service;

  @BeforeEach
  void setUp() {
    service = new ConcreteNamedEntityService(repository);
  }

  @Test
  void getByName_WhenExists_ShouldReturnDto() {
    TestEntity entity = new TestEntity(1L, "UniqueName", "Value");
    when(repository.findAll()).thenReturn(List.of(entity));

    TestDto result = service.getByName("UniqueName");

    assertThat(result.getName()).isEqualTo("UniqueName");
    verify(repository).findAll();
  }

  @Test
  void getByName_WhenNotFound_ShouldThrowException() {
    when(repository.findAll()).thenReturn(List.of());

    assertThatThrownBy(() -> service.getByName("NonExistent"))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(repository).findAll();
  }

  @Test
  void existsByName_ShouldReturnCorrectValue() {
    TestEntity entity = new TestEntity(1L, "ExistingName", "Value");
    when(repository.findAll()).thenReturn(List.of(entity));
    assertThat(service.existsByName("ExistingName")).isTrue();

    when(repository.findAll()).thenReturn(List.of());
    assertThat(service.existsByName("NonExistent")).isFalse();
  }

  @Test
  void create_WithUniqueName_ShouldSucceed() {
    TestDto input = new TestDto(null, "UniqueName", "Value");
    when(repository.findAll()).thenReturn(new ArrayList<>());
    when(repository.save(any(TestEntity.class))).thenReturn(new TestEntity(1L, "UniqueName", "Value"));

    TestDto result = service.create(input);

    assertThat(result.getId()).isEqualTo(1L);
    verify(repository).save(any(TestEntity.class));
  }

  @Test
  void create_WithDuplicateName_ShouldThrowException() {
    TestEntity existing = new TestEntity(1L, "DuplicateName", "Value");
    when(repository.findAll()).thenReturn(List.of(existing));

    TestDto input = new TestDto(null, "DuplicateName", "AnotherValue");

    assertThatThrownBy(() -> service.create(input))
        .isInstanceOf(DuplicateResourceException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void update_ToSameName_ShouldSucceed() {
    TestEntity existing = new TestEntity(1L, "SameName", "Value");
    when(repository.findById(1L)).thenReturn(Optional.of(existing));
    // REMOVED the unnecessary stub for findAll() since it won't be called when name doesn't change
    when(repository.save(any(TestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TestDto updateDto = new TestDto(1L, "SameName", "NewValue");
    TestDto result = service.update(1L, updateDto);

    assertThat(result.getName()).isEqualTo("SameName");
    assertThat(result.getValue()).isEqualTo("NewValue");
    verify(repository).findById(1L);
    verify(repository).save(any(TestEntity.class));
    // Verify that findAll was NOT called since name didn't change
    verify(repository, never()).findAll();
  }

  @Test
  void update_ToNewUniqueName_ShouldSucceed() {
    TestEntity existing = new TestEntity(1L, "OldName", "Value");
    when(repository.findById(1L)).thenReturn(Optional.of(existing));
    when(repository.findAll()).thenReturn(List.of(existing));
    when(repository.save(any(TestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TestDto updateDto = new TestDto(1L, "NewUniqueName", "NewValue");
    TestDto result = service.update(1L, updateDto);

    assertThat(result.getName()).isEqualTo("NewUniqueName");
    assertThat(result.getValue()).isEqualTo("NewValue");
    verify(repository).findById(1L);
    verify(repository).findAll();
    verify(repository).save(any(TestEntity.class));
  }

  @Test
  void update_ToExistingName_ShouldThrowException() {
    TestEntity entity1 = new TestEntity(1L, "FirstEntity", "Value1");
    TestEntity entity2 = new TestEntity(2L, "SecondEntity", "Value2");

    when(repository.findById(2L)).thenReturn(Optional.of(entity2));
    when(repository.findAll()).thenReturn(List.of(entity1, entity2));

    TestDto updateDto = new TestDto(2L, "FirstEntity", "NewValue");

    assertThatThrownBy(() -> service.update(2L, updateDto))
        .isInstanceOf(DuplicateResourceException.class);
    verify(repository).findById(2L);
    verify(repository).findAll();
    verify(repository, never()).save(any());
  }
}

// Concrete implementation of NamedEntityService
class ConcreteNamedEntityService extends NamedEntityService<TestEntity, TestDto, Long> {

  public ConcreteNamedEntityService(JpaRepository<TestEntity, Long> repository) {
    super(repository, "TestEntity",
        entity -> new TestDto(entity.getId(), entity.getName(), entity.getValue()),
        dto -> new TestEntity(dto.getId(), dto.getName(), dto.getValue()),
        name -> repository.findAll().stream()
            .filter(e -> name.equals(e.getName()))
            .findFirst(),
        null);

    // Fix the self reference
    try {
      java.lang.reflect.Field selfField = NamedEntityService.class.getDeclaredField("self");
      selfField.setAccessible(true);
      selfField.set(this, this);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Long getIdValue(TestEntity entity) {
    return entity.getId();
  }

  @Override
  protected String extractNameFromDto(TestDto dto) {
    return dto.getName();
  }

  @Override
  protected String extractNameFromEntity(TestEntity entity) {
    return entity.getName();
  }

  @Override
  protected void updateEntity(TestEntity entity, TestDto dto) {
    entity.setName(dto.getName());
    entity.setValue(dto.getValue());
  }
}