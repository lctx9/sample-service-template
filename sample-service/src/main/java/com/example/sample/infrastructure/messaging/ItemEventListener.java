package com.example.sample.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.common.event.EventEnvelope;
import com.example.sample.domain.ItemCreatedEvent;

/**
 * Sample consumer. In a real system the consumer is usually another service
 * (e.g. notification-service listens to user events).
 *
 * <p>RabbitMQ may deliver the same message more than once, so real handlers
 * must be idempotent (for example: skip an eventId that was already handled).
 */
@Component
public class ItemEventListener {

    private static final Logger log = LoggerFactory.getLogger(ItemEventListener.class);

    @RabbitListener(queues = RabbitConfig.ITEM_CREATED_QUEUE)
    public void onItemCreated(EventEnvelope<ItemCreatedEvent> envelope) {
        log.info("Received {} eventId={} itemId={} name={}",
                envelope.eventType(), envelope.eventId(),
                envelope.payload().itemId(), envelope.payload().name());
    }
}
