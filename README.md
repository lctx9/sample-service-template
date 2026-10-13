# Sample Service Template

A template for building **Spring Boot microservices**. It has one working sample service,
a shared library and an API gateway. Copy the sample service to start a new service,
so every service in the team has the same structure.

## What is inside

| Part | What it shows |
|---|---|
| **Layered structure** | `api` → `application` → `domain` ← `infrastructure` |
| **Common response format** | Every API returns `{ success, data, errorCode, message }` |
| **Global error handling** | Business errors, validation errors and unexpected errors all use the same format |
| **Database** | PostgreSQL + Spring Data JPA, tables created by Flyway |
| **Events** | Send and receive an event through RabbitMQ, with a common event format |
| **Health check** | `/actuator/health` (shows database and RabbitMQ status) |
| **API docs** | Swagger UI |
| **Tests** | Unit test (Mockito) and integration test (Testcontainers) |
| **Docker** | One Dockerfile for all modules, Docker Compose for the whole system |
| **API Gateway** | Single entry point, routes requests to services |

**Tech:** Java 21, Spring Boot 3.5, Spring Cloud Gateway, PostgreSQL 16, RabbitMQ 3.13, Maven.

---

## Project structure

```
sample-service-template/
├── common/                     # Shared library, used by every service
│   └── com/example/common/
│       ├── web/                # ApiResponse, PageResponse, GlobalExceptionHandler
│       ├── error/              # ErrorCode, CommonErrorCode, BusinessException
│       └── event/              # EventEnvelope (common event format)
│
├── sample-service/             # The sample service — copy this to create a new one
│   └── com/example/sample/
│       ├── api/                # Controllers + request/response DTOs
│       ├── application/        # Services: business logic
│       ├── domain/             # Entities, error codes, domain events
│       ├── infrastructure/
│       │   ├── persistence/    # Repositories
│       │   └── messaging/      # RabbitMQ config, publisher, listener
│       └── config/             # Other config (Swagger...)
│   └── resources/
│       ├── application.yml
│       └── db/migration/       # Flyway scripts
│
├── gateway/                    # API Gateway (port 8080)
├── Dockerfile                  # Builds any module: --build-arg MODULE=<module>
├── docker-compose.yml          # PostgreSQL, RabbitMQ, services, gateway
└── .env.example
```

### Layer rules

| Layer | Can use | Must not |
|---|---|---|
| `api` | `application`, DTOs | Call repositories directly, return entities |
| `application` | `domain`, `infrastructure` | Know about HTTP (no `ResponseEntity` here) |
| `domain` | Nothing outside the domain | Depend on Spring web or RabbitMQ |
| `infrastructure` | `domain` | Contain business rules |

---

## Run the project

### Requirements
- Java 21
- Docker Desktop (running)

### Option 1: Run everything with Docker

```bash
cp .env.example .env
docker compose up -d --build
```

| URL | What |
|---|---|
| http://localhost:8080/api/v1/items | API through the gateway |
| http://localhost:8081/swagger-ui.html | Swagger UI of sample-service |
| http://localhost:8081/actuator/health | Health check |
| http://localhost:15672 | RabbitMQ UI (guest / guest) |

```bash
docker compose logs -f sample-service   # view logs
docker compose down                     # stop
docker compose down -v                  # stop and delete all data
```

### Option 2: Run the service from the IDE

Start only the database and RabbitMQ in Docker, then run `SampleServiceApplication` in your IDE:

```bash
docker compose up -d postgres rabbitmq
./mvnw install -DskipTests            # build common first
./mvnw -pl sample-service spring-boot:run
```

### Try the API

```bash
# Create an item
curl -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "description": "A laptop"}'

# Get an item
curl http://localhost:8080/api/v1/items/1

# List items (paging and sorting)
curl "http://localhost:8080/api/v1/items?page=0&size=10&sort=name,asc"
```

After creating an item, check the `sample-service` logs: you will see the `ItemCreated` event
being sent and received.

### Run tests

```bash
./mvnw test
```

Integration tests need Docker. If Docker is not running, they are skipped.

---

## Conventions

### Response format

```json
// Success
{ "success": true, "data": { "id": 1, "name": "Laptop" }, "errorCode": null, "message": null }

// Error
{ "success": false, "data": null, "errorCode": "ITEM_NOT_FOUND", "message": "Item not found" }

// Validation error: data lists the invalid fields
{ "success": false, "data": { "name": "Name is required" }, "errorCode": "VALIDATION_ERROR", "message": "Invalid input data" }
```

### Errors

- Each service has its own `XxxErrorCode` enum that implements `ErrorCode`
- Name codes as `<THING>_<PROBLEM>`: `ITEM_NOT_FOUND`, `ORDER_ALREADY_PAID`
- Break a business rule → `throw new BusinessException(XxxErrorCode.SOMETHING)`. Do not build error responses by hand.

### Events

Every event uses `EventEnvelope`:

```json
{
  "eventId": "5f0c2b1e-...",
  "eventType": "ItemCreated",
  "version": 1,
  "occurredAt": "2026-10-13T10:00:00Z",
  "source": "sample-service",
  "payload": { "itemId": 1, "name": "Laptop" }
}
```

| Thing | Rule | Example |
|---|---|---|
| Event type | PascalCase, past tense | `ItemCreated`, `OrderPaid` |
| Exchange | `<service>.events` (topic) | `sample-service.events` |
| Routing key | `<thing>.<action>`, lower case | `item.created` |
| Queue | `<consumer-service>.<event>` | `notification-service.order-paid` |

- Events are sent **after the database transaction commits** (`@TransactionalEventListener(AFTER_COMMIT)`)
- Listeners must be **idempotent**: the same event can arrive more than once

### Database

- Tables are created only by Flyway scripts in `db/migration`, never by Hibernate (`ddl-auto: validate`)
- Script name: `V<number>__<what_it_does>.sql`, e.g. `V2__add_status_to_items.sql`
- Never edit a script that is already merged. Add a new one.
- Each service has its own database. Never read another service's database.

### Configuration

- Secrets (passwords, keys) come from environment variables, never from code
- Local values go in `.env` (not committed). Add new variables to `.env.example`.

---

## Create a new service from this template

Example: create `order-service`.

1. **Copy the module**
   Copy `sample-service/` to `order-service/`.

2. **Rename**
   - `order-service/pom.xml`: change `artifactId`, `name`, `description`
   - Root `pom.xml`: add `<module>order-service</module>`
   - Package: `com.example.sample` → `com.example.order`
   - Main class: `SampleServiceApplication` → `OrderServiceApplication`

3. **Update `application.yml`**
   - `spring.application.name: order-service`
   - New port (e.g. `8082`) and new database name (e.g. `order_db`)

4. **Remove the sample code**
   Delete `Item*` classes, `V1__create_items_table.sql` and the sample tests.
   Keep the folder structure, `RabbitConfig` pattern and `OpenApiConfig`.

5. **Add to Docker**
   - `Dockerfile`: add `COPY order-service/pom.xml order-service/`
   - `docker-compose.yml`: add an `order-service` block (copy `sample-service`), with its own database
     (add a new `postgres` service or a new database)

6. **Add a gateway route** in `gateway/src/main/resources/application.yml`:
   ```yaml
   - id: order-service
     uri: ${ORDER_SERVICE_URL:http://localhost:8082}
     predicates:
       - Path=/api/v1/orders/**
   ```
   And set `ORDER_SERVICE_URL: http://order-service:8082` for the gateway in `docker-compose.yml`.

7. **Check**
   - `./mvnw test` passes
   - `docker compose up -d --build`
   - `http://localhost:8082/actuator/health` → `UP`
   - Calling `/api/v1/orders/...` through `http://localhost:8080` works

---

## Not included yet

These are left out on purpose to keep the template small. Add them when your project needs them:

- Authentication (JWT) and permission checks
- Outbox pattern for events that must never be lost
- Centralized logging, tracing and metrics
- Service discovery (services are found by Docker Compose service names)
