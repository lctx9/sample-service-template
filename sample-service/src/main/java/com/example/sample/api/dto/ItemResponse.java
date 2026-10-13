package com.example.sample.api.dto;

import java.time.Instant;

import com.example.sample.domain.Item;

/**
 * What the API returns. Never return entities directly.
 */
public record ItemResponse(Long id, String name, String description, Instant createdAt) {

    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getName(), item.getDescription(), item.getCreatedAt());
    }
}
