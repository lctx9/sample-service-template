package com.example.sample.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ naming rules:
 * <ul>
 *   <li>Exchange: {@code <service>.events} — one topic exchange per service that sends events</li>
 *   <li>Routing key: {@code <thing>.<action>} in lower case, e.g. {@code item.created}</li>
 *   <li>Queue: {@code <consumer-service>.<event>}, e.g. {@code sample-service.item-created}</li>
 * </ul>
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "sample-service.events";
    public static final String ITEM_CREATED_ROUTING_KEY = "item.created";
    public static final String ITEM_CREATED_QUEUE = "sample-service.item-created";

    @Bean
    TopicExchange sampleEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue itemCreatedQueue() {
        return QueueBuilder.durable(ITEM_CREATED_QUEUE).build();
    }

    @Bean
    Binding itemCreatedBinding(Queue itemCreatedQueue, TopicExchange sampleEventsExchange) {
        return BindingBuilder.bind(itemCreatedQueue).to(sampleEventsExchange).with(ITEM_CREATED_ROUTING_KEY);
    }

    /** Send and receive messages as JSON (used by RabbitTemplate and @RabbitListener). */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
