package notificationsystem.channel;

import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;

public interface DeliveryChannel {
    NotificationType type();

    DeliveryReceipt deliver(NotificationRequest request);
}
