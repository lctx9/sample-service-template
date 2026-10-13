package com.example.sample.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sample.domain.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

    boolean existsByNameIgnoreCase(String name);
}
