package notificationsystem.decorator;

import java.util.Objects;
import java.util.Optional;
import notificationsystem.model.HtmlEscaper;
import notificationsystem.model.NotificationMessage;

public class SignatureDecorator implements NotificationMessageDecorator {
    private final NotificationMessage wrappedMessage;
    private final String signerName;

    public SignatureDecorator(NotificationMessage wrappedMessage, String signerName) {
        this.wrappedMessage = Objects.requireNonNull(wrappedMessage, "wrappedMessage cannot be null");
        this.signerName = requireText(signerName, "signerName");
    }

    @Override
    public NotificationMessage wrappedMessage() {
        return wrappedMessage;
    }

    @Override
    public String body() {
        return wrappedMessage().body() + "\n\nRegards,\n" + signerName;
    }

    @Override
    public Optional<String> htmlBody() {
        return wrappedMessage().htmlBody()
                .map(htmlBody -> htmlBody + "<br><br>Regards,<br>" + HtmlEscaper.escape(signerName));
    }

    private static String requireText(String value, String fieldName) {
        String checkedValue = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
        if (checkedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return checkedValue;
    }
}
