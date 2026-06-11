package notificationsystem.observer;

import notificationsystem.model.NotificationRequest;

public class NotificationLogObserver implements NotificationObserver {
    @Override
    public void update(NotificationRequest request) {
        System.out.println(
                "Audit log: notification "
                        + request.id()
                        + " queued for "
                        + request.type()
                        + " recipient "
                        + request.recipient());
    }
}
