package com.pricetracker.service.base;

import com.pricetracker.exception.DuplicateResourceException; // Добавлен импорт
import com.pricetracker.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;

public abstract class NamedEntityService<T, D, ID> extends BaseService<T, D, ID> {

  private final Function<String, Optional<T>> findByNameFunction;

  protected NamedEntityService(JpaRepository<T, ID> repository,
      String entityName,
      Function<T, D> toDto,
      Function<D, T> toEntity,
      Function<String, Optional<T>> findByNameFunction) {
    super(repository, entityName, toDto, toEntity);
    this.findByNameFunction = findByNameFunction;
  }

  @Transactional(readOnly = true)
  public D getByName(String name) {
    log.debug("Getting {} by name: {}", entityName, name);
    return findByNameFunction.apply(name)
        .map(toDto)
        .orElseThrow(() -> new ResourceNotFoundException(entityName, "name", name));
  }

  @Transactional(readOnly = true)
  public boolean existsByName(String name) {
    return findByNameFunction.apply(name).isPresent();
  }

  @Override
  protected void validateBeforeCreate(D dto) {
    String name = extractNameFromDto(dto);
    checkUnique(() -> existsByName(name), "name", name);
  }

  @Override
  protected void validateBeforeUpdate(ID id, D dto, T entity) {
    String newName = extractNameFromDto(dto);
    String oldName = extractNameFromEntity(entity);

    if (!oldName.equals(newName) && existsByName(newName)) {
      throw new DuplicateResourceException(entityName, "name", newName);
    }
  }

  protected abstract String extractNameFromDto(D dto);

  protected abstract String extractNameFromEntity(T entity);
}