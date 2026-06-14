# Event Driven Architecture Study Guide

This guide explains event driven architecture (EDA) using an e-commerce order
service as the running example. It is written for studying system design and
microservices design patterns, so it focuses on the "why", "when", and "how" of
each pattern.

## Table of Contents

1. [What Is Event Driven Architecture?](#what-is-event-driven-architecture)
2. [Why Use EDA in Microservices?](#why-use-eda-in-microservices)
3. [Running Example: E-commerce Order Platform](#running-example-e-commerce-order-platform)
4. [Core Event Driven Concepts](#core-event-driven-concepts)
5. [Order Service Event Flow](#order-service-event-flow)
6. [Microservices Design Patterns Used](#microservices-design-patterns-used)
7. [Event Design Guidelines](#event-design-guidelines)
8. [Reliability, Ordering, and Idempotency](#reliability-ordering-and-idempotency)
9. [Observability and Operations](#observability-and-operations)
10. [Common Tradeoffs and Anti-patterns](#common-tradeoffs-and-anti-patterns)
11. [Study Checklist](#study-checklist)

## What Is Event Driven Architecture?

Event driven architecture is an architectural style where services communicate
by producing and consuming events.

An event is a record that something meaningful already happened.

Examples:

- `OrderCreated`
- `PaymentAuthorized`
- `InventoryReserved`
- `OrderPacked`
- `OrderShipped`
- `OrderCancelled`

In an event driven system, the service that knows about a state change publishes
an event. Other services subscribe to the event and react independently.

### Synchronous request-response vs event driven communication

In a synchronous design, the order service might call payment, inventory,
shipping, notification, and analytics directly before returning a response to
the user.

```text
Client -> Order Service -> Payment Service -> Inventory Service -> Shipping Service
```

This is simple initially, but it tightly couples the order service to every
downstream service. A slow or failing dependency can block order creation.

In an event driven design, the order service persists the order and publishes an
event. Other services react asynchronously.

```text
Client -> Order Service -> OrderCreated event -> Payment / Inventory / Analytics
```

This reduces direct coupling and lets each service evolve more independently.

## Why Use EDA in Microservices?

EDA is useful when a business process spans multiple independently deployed
services.

In an e-commerce platform, one order can affect:

- Payments
- Inventory
- Shipping
- Notifications
- Fraud checks
- Loyalty points
- Search/read models
- Analytics
- Customer support views

If the order service directly calls all of these services, it becomes a central
orchestrator for too many responsibilities. Event driven communication lets the
order service focus on the order domain and lets other services subscribe to the
facts they care about.

### Benefits

1. Loose coupling
   - Producers do not need to know every consumer.
   - New consumers can be added without changing the producer.

2. Better scalability
   - Consumers can scale independently.
   - High-volume workloads can be buffered in a broker.

3. Resilience
   - A temporary failure in the notification service should not stop order
     creation.
   - Failed messages can be retried or sent to a dead letter queue.

4. Extensibility
   - Analytics, recommendation, and audit services can be added later by
     subscribing to existing events.

5. Auditability
   - Events provide a useful history of business facts.

### Costs

EDA is powerful, but it introduces complexity:

- Eventual consistency instead of immediate consistency.
- Duplicate event delivery.
- Message ordering concerns.
- Harder debugging across services.
- Schema evolution problems.
- More operational infrastructure.

Use EDA when the benefits of decoupling and asynchronous processing are worth
these costs.

## Running Example: E-commerce Order Platform

Assume an e-commerce system with these services:

| Service | Responsibility |
| --- | --- |
| Order Service | Owns order lifecycle and order state |
| Payment Service | Authorizes, captures, and refunds payments |
| Inventory Service | Reserves and releases stock |
| Shipping Service | Creates shipment and tracks delivery |
| Notification Service | Sends email, SMS, or push notifications |
| Customer Service | Owns customer profile and addresses |
| Product Service | Owns product catalog data |
| Analytics Service | Builds business metrics and reports |
| Search/Query Service | Builds read-optimized views for UI |

Important ownership rule:

Each service owns its own data. Other services should not directly read or write
that database.

For example:

- Order Service owns `orders`.
- Payment Service owns `payments`.
- Inventory Service owns `stock_reservations`.
- Shipping Service owns `shipments`.

This is the Database per Service pattern.

## Core Event Driven Concepts

### Event

An event is a fact that already happened.

Good event names are past tense:

- `OrderCreated`
- `PaymentAuthorized`
- `InventoryReservationFailed`

Poor event names are commands:

- `CreateOrder`
- `AuthorizePayment`
- `ReserveInventory`

Commands ask a service to do something. Events announce that something already
happened.

### Producer

A producer publishes events.

Example:

- Order Service produces `OrderCreated`.
- Payment Service produces `PaymentAuthorized`.
- Inventory Service produces `InventoryReserved`.

### Consumer

A consumer subscribes to events and reacts.

Example:

- Payment Service consumes `OrderCreated`.
- Notification Service consumes `OrderConfirmed`.
- Analytics Service consumes many order-related events.

### Broker

A broker stores and routes events between services.

Examples:

- Kafka
- RabbitMQ
- Amazon SNS/SQS
- Google Pub/Sub
- Azure Event Hubs

The broker decouples producers from consumers.

### Topic or Queue

A topic broadcasts events to multiple subscribers.

Example:

```text
orders.events topic
  - Payment Service subscribes
  - Inventory Service subscribes
  - Analytics Service subscribes
```

A queue distributes messages among competing consumers.

Example:

```text
payment-work queue
  - payment-worker-1
  - payment-worker-2
  - payment-worker-3
```

### Eventual Consistency

In event driven microservices, services do not update all data in one global
transaction. Instead, each service updates its own database and publishes events.
Other services update later.

For a short time:

- Order may be `PENDING_PAYMENT`.
- Payment may still be processing.
- Inventory may not yet be reserved.

The system eventually reaches a consistent state such as `CONFIRMED` or
`CANCELLED`.

## Order Service Event Flow

### High-level flow

```text
1. Customer places order
2. Order Service creates order with status PENDING
3. Order Service publishes OrderCreated
4. Payment Service authorizes payment
5. Inventory Service reserves stock
6. Order Service reacts to payment and inventory events
7. Order Service confirms or cancels order
8. Notification and Shipping services react to final order events
```

### Example choreography

```text
Client
  |
  v
Order Service
  |-- stores order as PENDING
  |-- publishes OrderCreated
        |
        +--> Payment Service
        |      |-- authorizes payment
        |      `-- publishes PaymentAuthorized or PaymentFailed
        |
        +--> Inventory Service
        |      |-- reserves stock
        |      `-- publishes InventoryReserved or InventoryReservationFailed
        |
        `--> Analytics Service
               `-- records order attempt

Order Service consumes:
  - PaymentAuthorized
  - PaymentFailed
  - InventoryReserved
  - InventoryReservationFailed

When payment and inventory are successful:
  Order Service publishes OrderConfirmed

When one step fails:
  Order Service publishes OrderCancelled
```

### Event examples

#### OrderCreated

```json
{
  "eventId": "evt-10001",
  "eventType": "OrderCreated",
  "eventVersion": 1,
  "occurredAt": "2026-06-14T13:00:00Z",
  "correlationId": "checkout-7788",
  "producer": "order-service",
  "data": {
    "orderId": "ORD-123",
    "customerId": "CUST-99",
    "items": [
      {
        "productId": "SKU-1",
        "quantity": 2,
        "unitPrice": 499
      }
    ],
    "currency": "USD",
    "totalAmount": 998,
    "shippingAddressId": "ADDR-10"
  }
}
```

#### PaymentAuthorized

```json
{
  "eventId": "evt-10002",
  "eventType": "PaymentAuthorized",
  "eventVersion": 1,
  "occurredAt": "2026-06-14T13:00:03Z",
  "correlationId": "checkout-7788",
  "producer": "payment-service",
  "data": {
    "orderId": "ORD-123",
    "paymentId": "PAY-456",
    "authorizedAmount": 998,
    "currency": "USD"
  }
}
```

#### InventoryReserved

```json
{
  "eventId": "evt-10003",
  "eventType": "InventoryReserved",
  "eventVersion": 1,
  "occurredAt": "2026-06-14T13:00:04Z",
  "correlationId": "checkout-7788",
  "producer": "inventory-service",
  "data": {
    "orderId": "ORD-123",
    "reservationId": "RES-777",
    "items": [
      {
        "productId": "SKU-1",
        "quantity": 2
      }
    ]
  }
}
```

## Microservices Design Patterns Used

This section lists the major design patterns used in the e-commerce order
example and explains each one clearly.

### 1. Database per Service Pattern

Each microservice owns its database. Other services do not directly access it.

In the example:

- Order Service owns order records.
- Payment Service owns payment records.
- Inventory Service owns stock and reservations.
- Shipping Service owns shipment records.

Why it is used:

- Preserves service autonomy.
- Allows each service to choose its own data model.
- Prevents tight database-level coupling.

Tradeoff:

- Cross-service joins are not available.
- Data must be replicated through events or queried through APIs.

Pattern used in the example:

```text
Order Service -> orders database
Payment Service -> payments database
Inventory Service -> inventory database
```

### 2. Event Notification Pattern

In this pattern, a service publishes a small event to announce that something
happened. Consumers may then fetch more details if needed.

Example:

```json
{
  "eventType": "OrderCreated",
  "data": {
    "orderId": "ORD-123"
  }
}
```

Why it is used:

- Keeps events small.
- Reduces duplication of large payloads.
- Useful when consumers can call the producer for details.

Tradeoff:

- Consumers may need extra API calls.
- Producer availability can affect consumers.

Where it fits:

- `OrderCreated` event with only `orderId`.
- Consumer calls Order Service if it needs full order details.

### 3. Event-Carried State Transfer Pattern

In this pattern, the event carries enough state for consumers to update their
own local views without calling the producer.

Example:

```json
{
  "eventType": "OrderCreated",
  "data": {
    "orderId": "ORD-123",
    "customerId": "CUST-99",
    "totalAmount": 998,
    "items": [
      {
        "productId": "SKU-1",
        "quantity": 2
      }
    ]
  }
}
```

Why it is used:

- Reduces synchronous calls between services.
- Improves consumer independence.
- Helps build read models and analytics pipelines.

Tradeoff:

- Events become larger.
- Schema evolution becomes more important.
- Sensitive data must be carefully controlled.

Where it fits:

- Analytics Service consumes full order data.
- Search/Query Service builds order history views.

### 4. Publish-Subscribe Pattern

In publish-subscribe, a producer publishes an event to a topic, and multiple
consumers receive it independently.

Example:

```text
OrderCreated
  -> Payment Service
  -> Inventory Service
  -> Analytics Service
  -> Customer Support Read Model
```

Why it is used:

- Producer does not know who consumes the event.
- New consumers can be added without changing Order Service.
- Multiple workflows can start from the same event.

Tradeoff:

- Harder to reason about all side effects.
- Requires good event catalog and observability.

### 5. Message Broker Pattern

A message broker sits between producers and consumers.

Example:

```text
Order Service -> Kafka topic orders.events -> Payment Service
```

Why it is used:

- Buffers messages during traffic spikes.
- Decouples producer and consumer availability.
- Supports retries and replay depending on broker.

Tradeoff:

- Adds infrastructure complexity.
- Broker configuration affects reliability and ordering.

### 6. Saga Pattern

A saga manages a business transaction that spans multiple services without a
single distributed database transaction.

For order checkout, the saga includes:

1. Create order.
2. Authorize payment.
3. Reserve inventory.
4. Confirm order.
5. If a later step fails, run compensating actions.

Compensating actions:

- If inventory fails after payment succeeds, release or void payment.
- If payment fails after inventory is reserved, release inventory.

There are two common saga styles: choreography and orchestration.

### 7. Saga Choreography Pattern

In choreography, services react to each other's events. There is no central
coordinator.

Example:

```text
OrderCreated -> Payment Service authorizes payment -> PaymentAuthorized
OrderCreated -> Inventory Service reserves stock -> InventoryReserved
PaymentAuthorized + InventoryReserved -> Order Service confirms order
```

Why it is used:

- Simple for workflows with few steps.
- Services remain loosely coupled.
- No central orchestrator service is required.

Tradeoff:

- Flow can become hard to understand as steps grow.
- Business process is spread across many services.
- Cycles and hidden dependencies can appear.

Used in the example:

- Order Service publishes `OrderCreated`.
- Payment and Inventory services independently react.
- Order Service listens for final success/failure events.

### 8. Saga Orchestration Pattern

In orchestration, a central orchestrator tells each service what to do next.

Example:

```text
Order Saga Orchestrator
  -> command: AuthorizePayment
  <- event: PaymentAuthorized
  -> command: ReserveInventory
  <- event: InventoryReserved
  -> command: ConfirmOrder
```

Why it is used:

- Easier to understand complex workflows.
- Central place for business process state.
- Better for long-running workflows with many branches.

Tradeoff:

- Orchestrator can become a central dependency.
- Services are less purely reactive.

Where it could fit:

- If checkout adds fraud review, split shipments, promotions, wallet payments,
  and manual approval, orchestration may be clearer than choreography.

### 9. Transactional Outbox Pattern

Problem:

Order Service must both:

1. Save the order in its database.
2. Publish `OrderCreated`.

If it saves the order but fails before publishing the event, other services will
not know about the order. If it publishes first but database save fails,
consumers may react to an order that does not exist.

Transactional Outbox solves this by saving the business record and event record
in the same local database transaction.

```text
Order Service transaction:
  - insert into orders
  - insert into outbox_events

Outbox Publisher:
  - reads unsent outbox events
  - publishes to broker
  - marks events as sent
```

Why it is used:

- Avoids dual-write inconsistency.
- Does not require a distributed transaction.
- Ensures events are eventually published.

Tradeoff:

- Requires an outbox table and publisher process.
- Consumers must still handle duplicates.

Used in the example:

- When order is created, Order Service writes both `orders` and
  `outbox_events` in one transaction.

### 10. Change Data Capture Pattern

Change Data Capture (CDC) reads database changes and publishes them as events.

Example:

```text
orders table change -> Debezium -> Kafka -> consumers
```

Why it is used:

- Reduces custom publisher code.
- Works well with the outbox pattern.
- Can reliably stream committed database changes.

Tradeoff:

- Requires CDC infrastructure.
- Database schema changes must be handled carefully.

Where it fits:

- A CDC tool reads the `outbox_events` table and publishes events to Kafka.

### 11. Idempotent Consumer Pattern

Most brokers provide at-least-once delivery. This means a consumer may receive
the same event more than once.

An idempotent consumer can process the same event repeatedly without incorrect
side effects.

Example:

Payment Service receives `OrderCreated` twice. It should not authorize payment
twice.

Implementation:

```text
processed_events table:
  - eventId
  - processedAt

Before processing:
  - check if eventId already processed
  - if yes, skip
  - if no, process and store eventId
```

Why it is used:

- Protects against duplicate messages.
- Makes retries safe.

Used in the example:

- Payment Service stores processed `OrderCreated.eventId`.
- Inventory Service stores processed `OrderCreated.eventId`.

### 12. Inbox Pattern

The Inbox pattern stores incoming messages before processing them.

```text
Consumer receives event
  -> writes event to inbox table
  -> processes event from inbox table
  -> marks as processed
```

Why it is used:

- Helps with retries.
- Gives an audit trail of received messages.
- Supports idempotency.

Tradeoff:

- More storage and processing logic.

Where it fits:

- Payment Service stores `OrderCreated` in its inbox before authorizing payment.

### 13. Dead Letter Queue Pattern

If a message repeatedly fails processing, it should not block the entire stream.
A dead letter queue stores messages that could not be processed after retries.

Example:

```text
InventoryReserved event fails 5 times -> send to inventory.dlq
```

Why it is used:

- Prevents poison messages from blocking consumers.
- Allows manual investigation.
- Keeps the main pipeline moving.

Tradeoff:

- DLQ messages need operational ownership.
- Ignoring DLQs can hide data loss.

### 14. Retry with Backoff Pattern

Temporary failures should be retried, but not immediately in a tight loop.

Example:

```text
Retry after 1 second
Retry after 5 seconds
Retry after 30 seconds
Send to DLQ after max attempts
```

Why it is used:

- Handles transient network or dependency failures.
- Prevents overloaded services from being hit repeatedly.

Used in the example:

- Notification Service retries failed email delivery.
- Shipping Service retries carrier API calls.

### 15. CQRS Pattern

Command Query Responsibility Segregation separates write models from read
models.

In the order platform:

- Order Service handles commands like "place order" and "cancel order".
- Query Service builds read-optimized views from events.

Example:

```text
OrderConfirmed event -> Query Service updates customer_order_history view
```

Why it is used:

- Write model can enforce business rules.
- Read model can be optimized for UI queries.
- Read models can combine data from many services.

Tradeoff:

- Read models are eventually consistent.
- More moving parts.

### 16. Event Sourcing Pattern

Event Sourcing stores the state of an entity as a sequence of events instead of
only storing the current state.

For an order:

```text
OrderCreated
PaymentAuthorized
InventoryReserved
OrderConfirmed
OrderShipped
```

The current order state is rebuilt by replaying events.

Why it is used:

- Complete audit history.
- Ability to rebuild projections.
- Helpful for complex domains where state transitions matter.

Tradeoff:

- More complex than CRUD.
- Event versioning is critical.
- Rebuilding state must be carefully optimized.

Where it may fit:

- Financial ledger, payment history, audit-heavy order lifecycle.

Where it may not be needed:

- A simple order CRUD system may only need normal tables plus integration
  events.

Important distinction:

- Event driven architecture does not require Event Sourcing.
- Event Sourcing is one possible pattern inside an event driven system.

### 17. API Gateway Pattern

An API Gateway provides a single entry point for clients.

Example:

```text
Web/Mobile Client -> API Gateway -> Order Service
```

Why it is used:

- Centralizes routing, authentication, rate limiting, and request shaping.
- Hides internal service topology from clients.

Tradeoff:

- Gateway can become too complex if business logic leaks into it.

Used in the example:

- Client submits checkout request through API Gateway to Order Service.

### 18. Backend for Frontend Pattern

A Backend for Frontend (BFF) creates client-specific APIs.

Example:

- Mobile BFF returns compact order status.
- Web BFF returns detailed order tracking and recommendations.

Why it is used:

- Avoids forcing all clients into one API shape.
- Keeps UI-specific composition outside core services.

Tradeoff:

- More services to maintain.

### 19. Circuit Breaker Pattern

Circuit Breaker prevents repeated calls to a failing dependency.

Example:

Shipping Service calls a carrier API. If the carrier is failing, the circuit
opens and calls fail fast or are queued for later.

Why it is used:

- Prevents cascading failures.
- Gives dependencies time to recover.

Used in the example:

- Shipping Service protects calls to external carrier APIs.
- Payment Service protects calls to external payment gateways.

### 20. Bulkhead Pattern

Bulkhead isolates resources so one failing workload does not exhaust the entire
service.

Example:

- Notification Service uses separate worker pools for email, SMS, and push.
- If email provider is slow, SMS workers are not exhausted.

Why it is used:

- Limits blast radius.
- Improves service resilience.

### 21. Strangler Fig Pattern

The Strangler Fig pattern incrementally replaces a legacy system with new
services.

Example:

An old monolithic order system publishes `OrderCreated` events. New payment,
inventory, and notification services are gradually introduced as consumers.

Why it is used:

- Supports gradual migration.
- Reduces risk compared with a big-bang rewrite.

### 22. Anti-Corruption Layer Pattern

An Anti-Corruption Layer (ACL) protects a service domain model from external or
legacy models.

Example:

Payment Service integrates with a third-party payment gateway. The gateway uses
fields like `txn_ref` and `auth_code`, but Payment Service translates these into
its own `PaymentAuthorized` model.

Why it is used:

- Keeps internal models clean.
- Reduces coupling to external systems.

## Event Design Guidelines

### Name events as facts

Use past tense:

- Good: `OrderCreated`
- Good: `PaymentFailed`
- Bad: `CreateOrder`
- Bad: `DoPayment`

### Include standard metadata

Every event should include:

| Field | Purpose |
| --- | --- |
| `eventId` | Unique event identifier for idempotency |
| `eventType` | Event name |
| `eventVersion` | Schema version |
| `occurredAt` | Time the business event happened |
| `publishedAt` | Time the event was published |
| `correlationId` | Connects events in one business flow |
| `causationId` | Identifies the event or command that caused this event |
| `producer` | Service that produced the event |

### Keep business meaning clear

An event should represent a meaningful business fact, not an internal technical
detail.

Good:

- `OrderConfirmed`
- `PaymentRefunded`

Bad:

- `OrderTableUpdated`
- `PaymentRowChanged`

### Version events carefully

Event schemas change over time. Consumers may not deploy at the same time as
producers.

Safe changes:

- Add optional fields.
- Add new event types.

Risky changes:

- Remove fields.
- Rename fields.
- Change field meaning.
- Change data type.

Recommended approach:

- Use `eventVersion`.
- Prefer backward-compatible changes.
- Keep old consumers working during rollout.
- Use a schema registry if the platform supports it.

### Do not put everything in every event

Balance event size and consumer independence.

Use event notification when:

- Payload would be too large.
- Consumers can fetch details.

Use event-carried state transfer when:

- Consumers need independence.
- Read models or analytics need data without synchronous calls.

## Reliability, Ordering, and Idempotency

### Delivery guarantees

Common broker delivery guarantees:

1. At most once
   - Message may be lost.
   - Message is not duplicated.

2. At least once
   - Message should not be lost.
   - Message may be duplicated.

3. Exactly once
   - Often limited to specific broker operations.
   - End-to-end exactly once across databases, APIs, and brokers is hard.

Most practical microservice systems assume at-least-once delivery and make
consumers idempotent.

### Ordering

Ordering is usually guaranteed only within a partition or queue.

For order events, use `orderId` as the partition key so all events for one order
go to the same partition.

```text
partition key = orderId

ORD-123 events:
  OrderCreated
  PaymentAuthorized
  InventoryReserved
  OrderConfirmed
```

This helps preserve ordering for one order without forcing global ordering for
all orders.

### Idempotency examples

Payment Service should handle duplicate `OrderCreated` events:

```text
if payment already exists for orderId:
  return existing payment result
else:
  authorize payment
```

Inventory Service should handle duplicate reservation requests:

```text
if reservation already exists for orderId:
  return existing reservation
else:
  reserve stock
```

Notification Service should handle duplicate `OrderConfirmed` events:

```text
if notification already sent for eventId:
  skip
else:
  send notification
```

### Handling failure in the order saga

Scenario: payment succeeds but inventory fails.

```text
OrderCreated
PaymentAuthorized
InventoryReservationFailed
PaymentRefundRequested
PaymentRefunded
OrderCancelled
```

Design patterns used:

- Saga Pattern for multi-service transaction.
- Compensating Transaction for refunding payment.
- Publish-Subscribe for event distribution.
- Idempotent Consumer for duplicate event handling.
- Transactional Outbox for reliable event publishing.

## Observability and Operations

EDA needs strong observability because one user action can cross many services.

### Correlation IDs

Use a `correlationId` across the entire checkout flow.

Example:

```text
correlationId = checkout-7788

OrderCreated
PaymentAuthorized
InventoryReserved
OrderConfirmed
NotificationSent
```

This lets logs, traces, and dashboards connect related events.

### Metrics to track

For each event type:

- Publish rate.
- Consumer lag.
- Processing latency.
- Retry count.
- DLQ count.
- Success/failure count.

For order checkout:

- Orders created per minute.
- Payment authorization failures.
- Inventory reservation failures.
- Order confirmation latency.
- Cancellation reasons.

### Distributed tracing

Use tracing to follow a request across:

```text
API Gateway -> Order Service -> Broker -> Payment Service -> Broker -> Order Service
```

Trace context should be propagated through event metadata.

### Event catalog

Maintain documentation for:

- Event name.
- Producer.
- Consumers.
- Schema.
- Version.
- Example payload.
- Retention policy.
- Ownership team.

This prevents event ecosystems from becoming confusing.

## Common Tradeoffs and Anti-patterns

### Anti-pattern: using events for every interaction

Not every interaction should be event driven.

Use synchronous APIs when:

- The caller needs an immediate answer.
- The operation is simple and local.
- Strong consistency is required.

Use events when:

- Something happened and many services may care.
- Work can happen asynchronously.
- Loose coupling matters.

### Anti-pattern: chatty events

Publishing too many low-level events creates noise.

Bad:

- `OrderFieldChanged`
- `OrderStatusColumnUpdated`
- `OrderCacheInvalidated`

Better:

- `OrderCreated`
- `OrderConfirmed`
- `OrderCancelled`

### Anti-pattern: shared database

If Payment Service reads the Order Service database directly, services become
tightly coupled.

Use APIs or events instead.

### Anti-pattern: no idempotency

If a duplicate event causes duplicate payment, duplicate shipment, or duplicate
notification, the design is unsafe.

Always assume consumers may receive duplicates.

### Anti-pattern: no owner for failed messages

A dead letter queue is not enough. Teams need alerts and runbooks for DLQ
messages.

## Pattern Map for the Order Service Example

| Pattern | Where it appears |
| --- | --- |
| Database per Service | Order, Payment, Inventory, Shipping each own data |
| Publish-Subscribe | `OrderCreated` is consumed by many services |
| Message Broker | Kafka/RabbitMQ/SNS-SQS routes events |
| Event Notification | Small events carrying identifiers |
| Event-Carried State Transfer | Order events carrying item and amount data |
| Saga | Checkout flow across order, payment, inventory |
| Saga Choreography | Services react to each other's events |
| Saga Orchestration | Alternative for complex checkout workflows |
| Transactional Outbox | Save order and event atomically |
| Change Data Capture | Publish outbox rows via CDC |
| Idempotent Consumer | Consumers deduplicate by `eventId` |
| Inbox | Consumers store incoming events before processing |
| Dead Letter Queue | Failed messages after retry exhaustion |
| Retry with Backoff | Temporary failure handling |
| CQRS | Query service builds read models from events |
| Event Sourcing | Optional audit-heavy order state model |
| API Gateway | Client entry point to order APIs |
| Backend for Frontend | Client-specific order views |
| Circuit Breaker | Protect calls to payment/carrier providers |
| Bulkhead | Isolate email/SMS/push worker pools |
| Strangler Fig | Gradual migration from monolith |
| Anti-Corruption Layer | Translate external payment gateway model |

## Study Checklist

Use this checklist to test your understanding:

- Can you explain the difference between a command and an event?
- Can you describe why `OrderCreated` should be past tense?
- Can you explain eventual consistency in checkout?
- Can you draw the order saga using events?
- Can you explain why distributed transactions are avoided?
- Can you explain Transactional Outbox and the dual-write problem?
- Can you explain why consumers must be idempotent?
- Can you compare saga choreography and saga orchestration?
- Can you explain CQRS and when to use it?
- Can you explain why Event Sourcing is optional, not required?
- Can you list what should go in event metadata?
- Can you describe how retries and DLQs work?
- Can you explain how correlation IDs help debugging?

## Summary

Event driven architecture helps microservices communicate through business facts
instead of direct dependencies. In an e-commerce order platform, events such as
`OrderCreated`, `PaymentAuthorized`, `InventoryReserved`, and `OrderConfirmed`
allow services to collaborate while staying independently deployable.

The most important patterns to understand are:

- Database per Service for ownership.
- Publish-Subscribe and Message Broker for event distribution.
- Saga for cross-service business transactions.
- Transactional Outbox for reliable publishing.
- Idempotent Consumer and Inbox for safe processing.
- Retry, Backoff, and DLQ for failure handling.
- CQRS and optional Event Sourcing for read models and audit-heavy domains.

Good EDA design is not just "use a broker". It requires careful event modeling,
clear ownership, reliable publishing, idempotent consumers, operational
visibility, and explicit handling of eventual consistency.
