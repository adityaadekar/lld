package notificationsystem.service;

import notificationsystem.decorator.MessageDecoration;
import notificationsystem.model.NotificationMessage;
import notificationsystem.model.NotificationType;

public interface NotificationService {
    static NotificationService createDefault() {
        return DefaultNotificationService.createDefault();
    }

    void send(NotificationType type, String recipient, NotificationMessage message, MessageDecoration... decorations);

    default void sendEmail(String recipient, NotificationMessage message, MessageDecoration... decorations) {
        send(NotificationType.EMAIL, recipient, message, decorations);
    }

    default void sendSms(String recipient, NotificationMessage message, MessageDecoration... decorations) {
        send(NotificationType.SMS, recipient, message, decorations);
    }

    default void sendPush(String recipient, NotificationMessage message, MessageDecoration... decorations) {
        send(NotificationType.PUSH, recipient, message, decorations);
    }
}
