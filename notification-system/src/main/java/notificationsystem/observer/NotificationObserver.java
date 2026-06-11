package notificationsystem.observer;

import notificationsystem.model.NotificationRequest;

public interface NotificationObserver {
    void update(NotificationRequest request);
}
