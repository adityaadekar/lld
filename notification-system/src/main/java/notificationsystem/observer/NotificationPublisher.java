package notificationsystem.observer;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import notificationsystem.model.NotificationRequest;

public class NotificationPublisher implements NotificationSubject {
    private final CopyOnWriteArrayList<NotificationObserver> observers = new CopyOnWriteArrayList<>();

    @Override
    public void register(NotificationObserver observer) {
        observers.addIfAbsent(Objects.requireNonNull(observer, "observer cannot be null"));
    }

    @Override
    public void unregister(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(NotificationRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        for (NotificationObserver observer : observers) {
            observer.update(request);
        }
    }

    public void publish(NotificationRequest request) {
        notifyObservers(request);
    }
}
