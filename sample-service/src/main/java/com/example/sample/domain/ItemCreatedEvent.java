package com.example.sample.domain;

/**
 * Domain event raised when an item is created. It is sent to RabbitMQ only after
 * the database transaction commits (see {@code ItemEventPublisher}).
 */
public record ItemCreatedEvent(Long itemId, String name) {

    public static final String TYPE = "ItemCreated";
}
