package notificationsystem.channel;

import java.time.Instant;
import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;

public class PushChannel implements DeliveryChannel {
    @Override
    public NotificationType type() {
        return NotificationType.PUSH;
    }

    @Override
    public DeliveryReceipt deliver(NotificationRequest request) {
        System.out.println("Sending PUSH notification to device " + request.recipient());
        System.out.println(request.message().subject());
        System.out.println(firstLine(request.message().body()));
        return new DeliveryReceipt(
                request.id(),
                type(),
                true,
                "Push notification accepted by provider",
                Instant.now());
    }

    private String firstLine(String body) {
        int firstLineBreak = body.indexOf('\n');
        if (firstLineBreak < 0) {
            return body;
        }
        return body.substring(0, firstLineBreak);
    }
}
