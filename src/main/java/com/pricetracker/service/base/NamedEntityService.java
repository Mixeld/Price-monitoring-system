package com.pricetracker.service.base;

import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class NamedEntityService<E, D, I> extends BaseService<E, D, I> {

  private final Function<String, Optional<E>> findByNameFunction;
  private final NamedEntityService<E, D, I> self;

  protected NamedEntityService(JpaRepository<E, I> repository,
      String entityName,
      Function<E, D> toDto,
      Function<D, E> toEntity,
      Function<String, Optional<E>> findByNameFunction,
      NamedEntityService<E, D, I> self) {
    super(repository, entityName, toDto, toEntity);
    this.findByNameFunction = findByNameFunction;
    this.self = self;
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
    checkUnique(() -> self.existsByName(name), "name", name);
  }

  @Override
  protected void validateBeforeUpdate(I id, D dto, E entity) {
    String newName = extractNameFromDto(dto);
    String oldName = extractNameFromEntity(entity);

    if (!oldName.equals(newName) && self.existsByName(newName)) {
      throw new DuplicateResourceException(entityName, "name", newName);
    }
  }

  protected abstract String extractNameFromDto(D dto);

  protected abstract String extractNameFromEntity(E entity);
}