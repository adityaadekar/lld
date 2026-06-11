package notificationsystem.channel;

import java.time.Instant;
import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;

public class EmailChannel implements DeliveryChannel {
    @Override
    public NotificationType type() {
        return NotificationType.EMAIL;
    }

    @Override
    public DeliveryReceipt deliver(NotificationRequest request) {
        System.out.println("Sending EMAIL to " + request.recipient());
        System.out.println("Subject: " + request.message().subject());
        System.out.println(request.message().body());
        return new DeliveryReceipt(
                request.id(),
                type(),
                true,
                "Email accepted by provider",
                Instant.now());
    }
}
