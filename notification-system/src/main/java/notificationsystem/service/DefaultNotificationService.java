package notificationsystem.service;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import notificationsystem.decorator.MessageDecoration;
import notificationsystem.model.NotificationMessage;
import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;
import notificationsystem.observer.EmailSubscriber;
import notificationsystem.observer.NotificationLogObserver;
import notificationsystem.observer.NotificationPublisher;
import notificationsystem.observer.PushSubscriber;
import notificationsystem.observer.SmsSubscriber;

final class DefaultNotificationService implements NotificationService {
    private final NotificationPublisher publisher;
    private final AtomicLong requestSequence;

    DefaultNotificationService(NotificationPublisher publisher) {
        this(publisher, 1001);
    }

    DefaultNotificationService(NotificationPublisher publisher, long firstRequestNumber) {
        this.publisher = Objects.requireNonNull(publisher, "publisher cannot be null");
        this.requestSequence = new AtomicLong(firstRequestNumber - 1);
    }

    static NotificationService createDefault() {
        NotificationPublisher publisher = new NotificationPublisher();
        publisher.register(new EmailSubscriber());
        publisher.register(new SmsSubscriber());
        publisher.register(new PushSubscriber());
        publisher.register(new NotificationLogObserver());
        return new DefaultNotificationService(publisher);
    }

    @Override
    public void send(
            NotificationType type,
            String recipient,
            NotificationMessage message,
            MessageDecoration... decorations) {
        NotificationMessage decoratedMessage = decorate(message, decorations);
        publisher.publish(new NotificationRequest(
                nextRequestId(),
                recipient,
                Objects.requireNonNull(type, "type cannot be null"),
                decoratedMessage,
                Collections.emptyMap()));
    }

    private NotificationMessage decorate(NotificationMessage message, MessageDecoration... decorations) {
        NotificationMessage decoratedMessage = Objects.requireNonNull(message, "message cannot be null");
        if (decorations == null) {
            return decoratedMessage;
        }
        for (MessageDecoration decoration : decorations) {
            decoratedMessage = Objects.requireNonNull(decoration, "decoration cannot be null").apply(decoratedMessage);
        }
        return decoratedMessage;
    }

    private String nextRequestId() {
        return "N-" + requestSequence.incrementAndGet();
    }
}
