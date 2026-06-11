package notificationsystem.channel;

import java.time.Instant;
import java.util.Objects;
import notificationsystem.model.NotificationType;

public class DeliveryReceipt {
    private final String notificationId;
    private final NotificationType channelType;
    private final boolean success;
    private final String detail;
    private final Instant deliveredAt;

    public DeliveryReceipt(
            String notificationId,
            NotificationType channelType,
            boolean success,
            String detail,
            Instant deliveredAt) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId cannot be null");
        this.channelType = Objects.requireNonNull(channelType, "channelType cannot be null");
        this.success = success;
        this.detail = Objects.requireNonNull(detail, "detail cannot be null");
        this.deliveredAt = Objects.requireNonNull(deliveredAt, "deliveredAt cannot be null");
    }

    public String notificationId() {
        return notificationId;
    }

    public NotificationType channelType() {
        return channelType;
    }

    public boolean success() {
        return success;
    }

    public String detail() {
        return detail;
    }

    public Instant deliveredAt() {
        return deliveredAt;
    }

    @Override
    public String toString() {
        return "DeliveryReceipt{"
                + "notificationId='" + notificationId + '\''
                + ", channelType=" + channelType
                + ", success=" + success
                + ", detail='" + detail + '\''
                + ", deliveredAt=" + deliveredAt
                + '}';
    }
}
