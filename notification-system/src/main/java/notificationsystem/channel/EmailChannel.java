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
        System.out.println("Text body: " + request.message().body());
        request.message().htmlBody()
                .ifPresent(htmlBody -> System.out.println("HTML body: " + htmlBody));
        return new DeliveryReceipt(
                request.id(),
                type(),
                true,
                "Email accepted by provider",
                Instant.now());
    }
}
