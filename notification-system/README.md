# Notification System LLD

This folder contains a low-level design for a notification system that supports
email, SMS, and push notifications. Email and push channels can deliver HTML
messages when a rich body is provided, while every message still includes a
plain-text fallback for channels that do not support HTML.

## Goals

- Publish a notification once and push it to interested subscribers.
- Use the Observer pattern for delivery fan-out.
- Use the Decorator pattern for dynamic message enrichment, such as adding a
  signature only when it is required.
- Prefer interfaces for behavioral contracts, using default interface methods
  where shared behavior does not require an abstract base class.
- Keep the design open for more optional behaviors like tracking footers,
  confidentiality notes, and urgency prefixes.

## Client API

Application/client code should stay small. It gets a configured
`NotificationService`, creates a message, passes a recipient, and lists the
decorations needed for that send:

```java
NotificationService notifications = NotificationService.createDefault();

notifications.sendEmail(
    "customer@example.com",
    NotificationMessages.html(
        "Payment received",
        "Your payment has been processed.",
        "<p>Your payment has been <strong>processed</strong>.</p>"
    ),
    MessageDecorations.signature("Billing Team"),
    MessageDecorations.tracking("PAY-1001")
);
```

The service hides request IDs, publisher/subscriber registration, channel
selection, and request construction. Those are internal system responsibilities,
not client responsibilities. `DefaultNotificationService` is package-internal
wiring behind the public `NotificationService` interface.

## Design Patterns Used

### Observer

`NotificationPublisher` is the subject. It stores registered
`NotificationObserver` instances and notifies them when a `NotificationRequest`
is published.

Concrete observers:

- `EmailSubscriber`
- `SmsSubscriber`
- `PushSubscriber`
- `NotificationLogObserver`

Channel subscribers receive every published notification but deliver only the
notification type they support.

### Decorator

`NotificationMessage` represents the message content. `PlainNotificationMessage`
contains plain text only, while `HtmlNotificationMessage` adds an optional HTML
body with the same plain-text fallback. Decorators wrap either message type when
optional behavior is required.

Decorators included:

- `SignatureDecorator`
- `TrackingFooterDecorator`
- `ConfidentialityFooterDecorator`
- `UrgencyPrefixDecorator`

Because decorators share the same `NotificationMessage` interface, the service
can compose them in any order:

```java
MessageDecoration decoration = MessageDecorations.signature("Billing Team");
```

HTML-capable messages can be sent through email and push channels:

```java
NotificationMessage message = NotificationMessages.html(
    "Payment received",
    "Your payment has been processed.",
    "<p>Your payment has been <strong>processed</strong>.</p>"
);
```

Decorator and subscriber extension points are modeled as interfaces:

- `NotificationMessageDecorator` supplies default delegation to a wrapped
  `NotificationMessage`.
- `ChannelSubscriber` supplies default observer delivery behavior for a
  `DeliveryChannel`.

## Class Structure

```text
notification-system
└── src/main/java/notificationsystem
    ├── Demo.java
    ├── channel
    │   ├── DeliveryChannel.java
    │   ├── DeliveryReceipt.java
    │   ├── EmailChannel.java
    │   ├── PushChannel.java
    │   └── SmsChannel.java
    ├── decorator
    │   ├── ConfidentialityFooterDecorator.java
    │   ├── MessageDecoration.java
    │   ├── MessageDecorations.java
    │   ├── NotificationMessageDecorator.java
    │   ├── SignatureDecorator.java
    │   ├── TrackingFooterDecorator.java
    │   └── UrgencyPrefixDecorator.java
    ├── model
    │   ├── HtmlEscaper.java
    │   ├── HtmlNotificationMessage.java
    │   ├── NotificationMessage.java
    │   ├── NotificationMessages.java
    │   ├── NotificationRequest.java
    │   ├── NotificationType.java
    │   └── PlainNotificationMessage.java
    ├── observer
        ├── ChannelSubscriber.java
        ├── EmailSubscriber.java
        ├── NotificationLogObserver.java
        ├── NotificationObserver.java
        ├── NotificationPublisher.java
        ├── NotificationSubject.java
        ├── PushSubscriber.java
        └── SmsSubscriber.java
    └── service
        ├── DefaultNotificationService.java
        └── NotificationService.java
```

## Main Flow

1. Client code obtains a configured `NotificationService`.
2. Client code sends a recipient, message, and optional decorations.
3. The service applies decorations and creates a `NotificationRequest`.
4. The service publishes the request through the configured publisher.
5. Matching channel subscriber delivers the notification.
6. Other observers, such as logging, can react independently.

## Running the Demo

From the repository root:

```bash
javac -d /tmp/notification-system-classes $(rg --files notification-system/src/main/java -g '*.java')
java -cp /tmp/notification-system-classes notificationsystem.Demo
```

Run the lightweight test harness:

```bash
javac -d /tmp/notification-system-classes $(rg --files notification-system/src/main/java notification-system/src/test/java -g '*.java')
java -cp /tmp/notification-system-classes notificationsystem.NotificationSystemTest
```
