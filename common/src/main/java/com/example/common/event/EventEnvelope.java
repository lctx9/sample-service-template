package com.example.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Common format for every event sent through RabbitMQ.
 *
 * <pre>
 * {
 *   "eventId":    "5f0c...",          // unique id, used to skip duplicate events
 *   "eventType":  "ItemCreated",      // PascalCase, past tense
 *   "version":    1,                  // increase when the payload changes in a breaking way
 *   "occurredAt": "2026-10-13T10:00:00Z",
 *   "source":     "sample-service",   // service that sent the event
 *   "payload":    { ... }             // event data
 * }
 * </pre>
 */
public record EventEnvelope<T>(
        String eventId,
        String eventType,
        int version,
        Instant occurredAt,
        String source,
        T payload) {

    public static <T> EventEnvelope<T> of(String eventType, String source, T payload) {
        return new EventEnvelope<>(UUID.randomUUID().toString(), eventType, 1, Instant.now(), source, payload);
    }
}
