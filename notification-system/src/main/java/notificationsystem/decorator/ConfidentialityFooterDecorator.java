package notificationsystem.decorator;

import java.util.Objects;
import java.util.Optional;
import notificationsystem.model.NotificationMessage;

public class ConfidentialityFooterDecorator implements NotificationMessageDecorator {
    private static final String CONFIDENTIALITY_NOTE =
            "This message may contain confidential information intended only for the recipient.";

    private final NotificationMessage wrappedMessage;

    public ConfidentialityFooterDecorator(NotificationMessage wrappedMessage) {
        this.wrappedMessage = Objects.requireNonNull(wrappedMessage, "wrappedMessage cannot be null");
    }

    @Override
    public NotificationMessage wrappedMessage() {
        return wrappedMessage;
    }

    @Override
    public String body() {
        return wrappedMessage().body() + "\n\n" + CONFIDENTIALITY_NOTE;
    }

    @Override
    public Optional<String> htmlBody() {
        return wrappedMessage().htmlBody()
                .map(htmlBody -> htmlBody + "<br><br><em>" + CONFIDENTIALITY_NOTE + "</em>");
    }
}
