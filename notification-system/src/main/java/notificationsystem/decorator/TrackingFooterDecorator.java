package notificationsystem.decorator;

import java.util.Objects;
import notificationsystem.model.NotificationMessage;

public class TrackingFooterDecorator extends NotificationMessageDecorator {
    private final String trackingId;

    public TrackingFooterDecorator(NotificationMessage wrappedMessage, String trackingId) {
        super(wrappedMessage);
        this.trackingId = requireText(trackingId, "trackingId");
    }

    @Override
    public String body() {
        return wrappedMessage().body() + "\n\nTracking ID: " + trackingId;
    }

    private static String requireText(String value, String fieldName) {
        String checkedValue = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
        if (checkedValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return checkedValue;
    }
}
