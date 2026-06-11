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

Because decorators share the same `NotificationMessage` interface, callers can
compose them in any order:

```java
NotificationMessage message = new PlainNotificationMessage(
    "Payment received",
    "Your payment has been processed."
);

message = new SignatureDecorator(message, "Billing Team");
message = new TrackingFooterDecorator(message, "PAY-1001");
```

HTML-capable messages can be sent through email and push channels:

```java
NotificationMessage message = new HtmlNotificationMessage(
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
    │   ├── NotificationMessageDecorator.java
    │   ├── SignatureDecorator.java
    │   ├── TrackingFooterDecorator.java
    │   └── UrgencyPrefixDecorator.java
    ├── model
    │   ├── HtmlEscaper.java
    │   ├── HtmlNotificationMessage.java
    │   ├── NotificationMessage.java
    │   ├── NotificationRequest.java
    │   ├── NotificationType.java
    │   └── PlainNotificationMessage.java
    └── observer
        ├── ChannelSubscriber.java
        ├── EmailSubscriber.java
        ├── NotificationLogObserver.java
        ├── NotificationObserver.java
        ├── NotificationPublisher.java
        ├── NotificationSubject.java
        ├── PushSubscriber.java
        └── SmsSubscriber.java
```

## Main Flow

1. Build a `NotificationMessage`.
2. Wrap it with decorators only for the current scenario.
3. Create a `NotificationRequest` with recipient, type, message, and metadata.
4. Register observers with `NotificationPublisher`.
5. Publish the request.
6. Matching channel subscriber delivers the notification.
7. Other observers, such as logging, can react independently.

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
