package notificationsystem.model;

import java.util.Objects;

public class PlainNotificationMessage implements NotificationMessage {
    private final String subject;
    private final String body;

    public PlainNotificationMessage(String subject, String body) {
        this.subject = requireText(subject, "subject");
        this.body = requireText(body, "body");
    }

    @Override
    public String subject() {
        return subject;
    }

    @Override
    public String body() {
        return body;
    }

    private static String requireText(String value, String fieldName) {
        String checkedValue = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
        if (checkedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return checkedValue;
    }
}
