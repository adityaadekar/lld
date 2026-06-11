package notificationsystem.model;

import java.util.Objects;
import java.util.Optional;

public class HtmlNotificationMessage implements NotificationMessage {
    private final String subject;
    private final String body;
    private final String htmlBody;

    public HtmlNotificationMessage(String subject, String body, String htmlBody) {
        this.subject = requireText(subject, "subject");
        this.body = requireText(body, "body");
        this.htmlBody = requireText(htmlBody, "htmlBody");
    }

    @Override
    public String subject() {
        return subject;
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public Optional<String> htmlBody() {
        return Optional.of(htmlBody);
    }

    private static String requireText(String value, String fieldName) {
        String checkedValue = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
        if (checkedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return checkedValue;
    }
}
