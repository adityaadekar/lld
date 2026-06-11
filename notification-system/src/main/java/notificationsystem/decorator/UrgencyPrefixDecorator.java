package notificationsystem.decorator;

import notificationsystem.model.NotificationMessage;

public class UrgencyPrefixDecorator extends NotificationMessageDecorator {
    public UrgencyPrefixDecorator(NotificationMessage wrappedMessage) {
        super(wrappedMessage);
    }

    @Override
    public String subject() {
        return "[URGENT] " + wrappedMessage().subject();
    }
}
