package com.pricetracker.service.base;

import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public abstract class BaseService<T, D, ID> {

  protected final JpaRepository<T, ID> repository;  // protected
  protected final String entityName;                 // protected
  protected final Function<T, D> toDto;              // protected
  protected final Function<D, T> toEntity;           // protected
  protected final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass()); // Добавить явный логгер

  protected BaseService(JpaRepository<T, ID> repository,
      String entityName,
      Function<T, D> toDto,
      Function<D, T> toEntity) {
    this.repository = repository;
    this.entityName = entityName;
    this.toDto = toDto;
    this.toEntity = toEntity;
  }

  @Transactional(readOnly = true)
  public List<D> getAll() {
    log.debug("Getting all {}", entityName + "s");
    return repository.findAll().stream()
        .map(toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public D getById(ID id) {
    log.debug("Getting {} by id: {}", entityName, id);
    return findEntityById(id)
        .map(toDto)
        .orElseThrow(() -> new ResourceNotFoundException(entityName, "id", id));
  }

  @Transactional
  public D create(D dto) {
    log.debug("Creating new {} with data: {}", entityName, dto);
    validateBeforeCreate(dto);

    T entity = toEntity.apply(dto);
    beforeSave(entity);
    T savedEntity = repository.save(entity);
    afterSave(savedEntity);

    log.info("{} created successfully with id: {}", entityName, getIdValue(savedEntity));
    return toDto.apply(savedEntity);
  }

  @Transactional
  public D update(ID id, D dto) {
    log.debug("Updating {} with id: {}", entityName, id);

    T entity = findEntityById(id)
        .orElseThrow(() -> new ResourceNotFoundException(entityName, "id", id));

    validateBeforeUpdate(id, dto, entity);
    updateEntity(entity, dto);
    beforeUpdate(entity);

    log.info("{} updated successfully with id: {}", entityName, id);
    return toDto.apply(entity);
  }

  @Transactional
  public void delete(ID id) {
    log.debug("Deleting {} with id: {}", entityName, id);

    T entity = findEntityById(id)
        .orElseThrow(() -> new ResourceNotFoundException(entityName, "id", id));

    validateBeforeDelete(entity);
    beforeDelete(entity);
    repository.delete(entity);
    afterDelete();

    log.info("{} deleted successfully with id: {}", entityName, id);
  }

  @Transactional(readOnly = true)
  public boolean existsById(ID id) {
    return repository.existsById(id);
  }

  protected Optional<T> findEntityById(ID id) {
    return repository.findById(id);
  }

  protected void checkUnique(Supplier<Boolean> existsCheck, String fieldName, Object value) {
    if (existsCheck.get()) {
      throw new DuplicateResourceException(entityName, fieldName, value);
    }
  }

  // Hook methods for subclasses
  protected void validateBeforeCreate(D dto) {}

  protected void validateBeforeUpdate(ID id, D dto, T entity) {}

  protected void validateBeforeDelete(T entity) {}

  protected void updateEntity(T entity, D dto) {}

  protected void beforeSave(T entity) {}

  protected void afterSave(T entity) {}

  protected void beforeUpdate(T entity) {}

  protected void beforeDelete(T entity) {}

  protected void afterDelete() {}

  protected abstract ID getIdValue(T entity);
}