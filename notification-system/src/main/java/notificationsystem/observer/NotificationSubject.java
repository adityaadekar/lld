package notificationsystem.observer;

import notificationsystem.model.NotificationRequest;

public interface NotificationSubject {
    void register(NotificationObserver observer);

    void unregister(NotificationObserver observer);

    void notifyObservers(NotificationRequest request);
}
