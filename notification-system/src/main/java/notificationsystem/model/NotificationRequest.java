package notificationsystem.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NotificationRequest {
    private final String id;
    private final String recipient;
    private final NotificationType type;
    private final NotificationMessage message;
    private final Map<String, String> metadata;

    public NotificationRequest(
            String id,
            String recipient,
            NotificationType type,
            NotificationMessage message,
            Map<String, String> metadata) {
        this.id = requireText(id, "id");
        this.recipient = requireText(recipient, "recipient");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.message = Objects.requireNonNull(message, "message cannot be null");
        this.metadata = copyMetadata(metadata);
    }

    public String id() {
        return id;
    }

    public String recipient() {
        return recipient;
    }

    public NotificationType type() {
        return type;
    }

    public NotificationMessage message() {
        return message;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    private static Map<String, String> copyMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    private static String requireText(String value, String fieldName) {
        String checkedValue = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
        if (checkedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return checkedValue;
    }
}
