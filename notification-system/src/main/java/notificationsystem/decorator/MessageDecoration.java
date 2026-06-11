package notificationsystem.decorator;

import notificationsystem.model.NotificationMessage;

public interface MessageDecoration {
    NotificationMessage apply(NotificationMessage message);
}
