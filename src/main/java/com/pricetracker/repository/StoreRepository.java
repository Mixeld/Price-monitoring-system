package com.pricetracker.repository;

import java.util.List;
import java.util.Optional;
import com.pricetracker.entity.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

  Optional<Store> findByName(String name);

  Optional<Store> findByWebsiteUrl(String websiteUrl);

  List<Store> findByNameContainingIgnoreCase(String namePattern);

  @Override
  @EntityGraph(attributePaths = {"priceHistories"})
  List<Store> findAll();

  @EntityGraph(attributePaths = {"priceHistories"})
  Optional<Store> findWithHistoriesById(Long id);
}