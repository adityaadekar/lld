package notificationsystem.channel;

import java.time.Instant;
import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;

public class SmsChannel implements DeliveryChannel {
    @Override
    public NotificationType type() {
        return NotificationType.SMS;
    }

    @Override
    public DeliveryReceipt deliver(NotificationRequest request) {
        System.out.println("Sending SMS to " + request.recipient());
        System.out.println(request.message().subject() + ": " + compactBody(request.message().body()));
        return new DeliveryReceipt(
                request.id(),
                type(),
                true,
                "SMS accepted by provider",
                Instant.now());
    }

    private String compactBody(String body) {
        return body.replace('\n', ' ');
    }
}
