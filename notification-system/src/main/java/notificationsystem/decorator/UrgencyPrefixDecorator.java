package notificationsystem.decorator;

import java.util.Objects;
import notificationsystem.model.NotificationMessage;

public class UrgencyPrefixDecorator implements NotificationMessageDecorator {
    private final NotificationMessage wrappedMessage;

    public UrgencyPrefixDecorator(NotificationMessage wrappedMessage) {
        this.wrappedMessage = Objects.requireNonNull(wrappedMessage, "wrappedMessage cannot be null");
    }

    @Override
    public NotificationMessage wrappedMessage() {
        return wrappedMessage;
    }

    @Override
    public String subject() {
        return "[URGENT] " + wrappedMessage().subject();
    }
}
