package com.example.sample.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.common.event.EventEnvelope;
import com.example.sample.domain.ItemCreatedEvent;

/**
 * Sends domain events to RabbitMQ.
 *
 * <p>It runs AFTER_COMMIT, so we never send an event for data that was rolled back.
 * Note: if RabbitMQ is down at that moment, the event is lost. When a flow cannot lose
 * events (orders, payments), use the Outbox pattern instead.
 */
@Component
public class ItemEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ItemEventPublisher.class);
    private static final String SOURCE = "sample-service";

    private final RabbitTemplate rabbitTemplate;

    public ItemEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onItemCreated(ItemCreatedEvent event) {
        EventEnvelope<ItemCreatedEvent> envelope = EventEnvelope.of(ItemCreatedEvent.TYPE, SOURCE, event);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ITEM_CREATED_ROUTING_KEY, envelope);
        log.info("Sent {} eventId={} itemId={}", envelope.eventType(), envelope.eventId(), event.itemId());
    }
}
