package notificationsystem.decorator;

import notificationsystem.model.NotificationMessage;

public class ConfidentialityFooterDecorator extends NotificationMessageDecorator {
    private static final String CONFIDENTIALITY_NOTE =
            "This message may contain confidential information intended only for the recipient.";

    public ConfidentialityFooterDecorator(NotificationMessage wrappedMessage) {
        super(wrappedMessage);
    }

    @Override
    public String body() {
        return wrappedMessage().body() + "\n\n" + CONFIDENTIALITY_NOTE;
    }
}
