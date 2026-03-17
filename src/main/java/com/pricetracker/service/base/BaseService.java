package com.pricetracker.service.base;

import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class BaseService<E, D, I> {

  protected final JpaRepository<E, I> repository;
  protected final String entityName;
  protected final Function<E, D> toDto;
  protected final Function<D, E> toEntity;
  protected final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

  protected BaseService(JpaRepository<E, I> repository,
      String entityName,
      Function<E, D> toDto,
      Function<D, E> toEntity) {
    this.repository = repository;
    this.entityName = entityName;
    this.toDto = toDto;
    this.toEntity = toEntity;
  }

  @Transactional(readOnly = true)
  public List<D> getAll() {
    log.debug("Getting all {}s", entityName);
    return repository.findAll().stream()
        .map(toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public D getById(I id) {
    log.debug("Getting {} by id: {}", entityName, id);
    return findEntityById(id)
        .map(toDto)
        .orElseThrow(() -> new ResourceNotFoundException(entityName, "id", id));
  }

  @Transactional
  public D create(D dto) {
    log.debug("Creating new {} with data: {}", entityName, dto);
    validateBeforeCreate(dto);

    E entity = toEntity.apply(dto);
    beforeSave(entity);
    E savedEntity = repository.save(entity);
    afterSave(savedEntity);

    log.info("{} created successfully with id: {}", entityName, getIdValue(savedEntity));
    return toDto.apply(savedEntity);
  }

  @Transactional
  public D update(I id, D dto) {
    log.debug("Updating {} with id: {}", entityName, id);

    E entity = findEntityById(id)
        .orElseThrow(() -> new ResourceNotFoundException(entityName, "id", id));

    validateBeforeUpdate(id, dto, entity);
    updateEntity(entity, dto);
    beforeUpdate(entity);

    log.info("{} updated successfully with id: {}", entityName, id);
    return toDto.apply(entity);
  }

  @Transactional
  public void delete(I id) {
    log.debug("Deleting {} with id: {}", entityName, id);

    E entity = findEntityById(id)
        .orElseThrow(() -> new ResourceNotFoundException(entityName, "id", id));

    validateBeforeDelete(entity);
    beforeDelete(entity);
    repository.delete(entity);
    afterDelete();

    log.info("{} deleted successfully with id: {}", entityName, id);
  }

  @Transactional(readOnly = true)
  public boolean existsById(I id) {
    return repository.existsById(id);
  }

  protected Optional<E> findEntityById(I id) {
    return repository.findById(id);
  }

  protected void checkUnique(BooleanSupplier existsCheck, String fieldName, Object value) {
    if (existsCheck.getAsBoolean()) {
      throw new DuplicateResourceException(entityName, fieldName, value);
    }
  }


  protected void validateBeforeCreate(D dto) {}

  protected void validateBeforeUpdate(I id, D dto, E entity) {}

  protected void validateBeforeDelete(E entity) {}

  protected void updateEntity(E entity, D dto) {}

  protected void beforeSave(E entity) {}

  protected void afterSave(E entity) {}

  protected void beforeUpdate(E entity) {}

  protected void beforeDelete(E entity) {}

  protected void afterDelete() {}

  protected abstract I getIdValue(E entity);
}