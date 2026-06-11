package notificationsystem.decorator;

import java.util.Objects;
import notificationsystem.model.NotificationMessage;

public class SignatureDecorator extends NotificationMessageDecorator {
    private final String signerName;

    public SignatureDecorator(NotificationMessage wrappedMessage, String signerName) {
        super(wrappedMessage);
        this.signerName = requireText(signerName, "signerName");
    }

    @Override
    public String body() {
        return wrappedMessage().body() + "\n\nRegards,\n" + signerName;
    }

    private static String requireText(String value, String fieldName) {
        String checkedValue = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
        if (checkedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return checkedValue;
    }
}
