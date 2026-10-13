package com.example.sample.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.sample.infrastructure.messaging.RabbitConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration test sample: start the real app with a real PostgreSQL and RabbitMQ
 * (in Docker, via Testcontainers) and call the API over HTTP.
 * Skipped automatically when Docker is not running.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class ItemApiIntegrationTest {

    private static final String TEST_QUEUE = "test.item-created";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void bindTestQueue() {
        // Our own queue, so the test can read the event without racing the app's listener
        Queue queue = new Queue(TEST_QUEUE, false, false, true);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(BindingBuilder.bind(queue)
                .to(new TopicExchange(RabbitConfig.EXCHANGE))
                .with(RabbitConfig.ITEM_CREATED_ROUTING_KEY));
        amqpAdmin.purgeQueue(TEST_QUEUE, false);
    }

    @Test
    void createItem_validRequest_returns201AndSendsEvent() throws Exception {
        ResponseEntity<JsonNode> response = rest.postForEntity(
                "/api/v1/items", Map.of("name", "Laptop", "description", "A laptop"), JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("success").asBoolean()).isTrue();
        assertThat(response.getBody().at("/data/name").asText()).isEqualTo("Laptop");

        Message message = rabbitTemplate.receive(TEST_QUEUE, 5000);
        assertThat(message).isNotNull();
        JsonNode event = objectMapper.readTree(message.getBody());
        assertThat(event.get("eventType").asText()).isEqualTo("ItemCreated");
        assertThat(event.get("source").asText()).isEqualTo("sample-service");
        assertThat(event.at("/payload/name").asText()).isEqualTo("Laptop");
    }

    @Test
    void createItem_blankName_returns400WithFieldError() {
        ResponseEntity<JsonNode> response = rest.postForEntity(
                "/api/v1/items", Map.of("name", ""), JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("errorCode").asText()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().at("/data/name").asText()).isEqualTo("Name is required");
    }

    @Test
    void createItem_duplicatedName_returns409() {
        rest.postForEntity("/api/v1/items", Map.of("name", "Phone"), JsonNode.class);

        ResponseEntity<JsonNode> response = rest.postForEntity(
                "/api/v1/items", Map.of("name", "phone"), JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("errorCode").asText()).isEqualTo("ITEM_NAME_DUPLICATED");
    }

    @Test
    void getItem_unknownId_returns404() {
        ResponseEntity<JsonNode> response = rest.getForEntity("/api/v1/items/999999", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("success").asBoolean()).isFalse();
        assertThat(response.getBody().get("errorCode").asText()).isEqualTo("ITEM_NOT_FOUND");
    }

    @Test
    void health_returnsUp() {
        ResponseEntity<JsonNode> response = rest.getForEntity("/actuator/health", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status").asText()).isEqualTo("UP");
    }
}
