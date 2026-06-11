package notificationsystem.decorator;

import java.util.Objects;
import notificationsystem.model.NotificationMessage;

public abstract class NotificationMessageDecorator implements NotificationMessage {
    private final NotificationMessage wrappedMessage;

    protected NotificationMessageDecorator(NotificationMessage wrappedMessage) {
        this.wrappedMessage = Objects.requireNonNull(wrappedMessage, "wrappedMessage cannot be null");
    }

    protected NotificationMessage wrappedMessage() {
        return wrappedMessage;
    }

    @Override
    public String subject() {
        return wrappedMessage.subject();
    }

    @Override
    public String body() {
        return wrappedMessage.body();
    }
}
