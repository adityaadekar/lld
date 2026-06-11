package notificationsystem;

import java.util.HashMap;
import java.util.Map;
import notificationsystem.decorator.ConfidentialityFooterDecorator;
import notificationsystem.decorator.SignatureDecorator;
import notificationsystem.decorator.TrackingFooterDecorator;
import notificationsystem.decorator.UrgencyPrefixDecorator;
import notificationsystem.model.HtmlNotificationMessage;
import notificationsystem.model.NotificationMessage;
import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;
import notificationsystem.model.PlainNotificationMessage;
import notificationsystem.observer.EmailSubscriber;
import notificationsystem.observer.NotificationLogObserver;
import notificationsystem.observer.NotificationPublisher;
import notificationsystem.observer.PushSubscriber;
import notificationsystem.observer.SmsSubscriber;

public final class Demo {
    private Demo() {
    }

    public static void main(String[] args) {
        NotificationPublisher publisher = new NotificationPublisher();
        publisher.register(new EmailSubscriber());
        publisher.register(new SmsSubscriber());
        publisher.register(new PushSubscriber());
        publisher.register(new NotificationLogObserver());

        publisher.publish(signedEmailRequest());
        printSeparator();

        publisher.publish(plainSmsRequest());
        printSeparator();

        publisher.publish(urgentPushRequest());
    }

    private static NotificationRequest signedEmailRequest() {
        NotificationMessage message = new HtmlNotificationMessage(
                "Invoice generated",
                "Your invoice for this billing cycle is ready.",
                "<p>Your invoice for this billing cycle is <strong>ready</strong>.</p>");
        message = new SignatureDecorator(message, "Finance Team");
        message = new TrackingFooterDecorator(message, "INV-2026-001");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("template", "invoice");
        metadata.put("requiresSignature", "true");

        return new NotificationRequest(
                "N-1001",
                "customer@example.com",
                NotificationType.EMAIL,
                message,
                metadata);
    }

    private static NotificationRequest plainSmsRequest() {
        NotificationMessage message = new PlainNotificationMessage(
                "OTP",
                "Your one-time password is 482913.");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("template", "otp");
        metadata.put("requiresSignature", "false");

        return new NotificationRequest(
                "N-1002",
                "+15550101010",
                NotificationType.SMS,
                message,
                metadata);
    }

    private static NotificationRequest urgentPushRequest() {
        NotificationMessage message = new HtmlNotificationMessage(
                "Security alert",
                "A new device signed in to your account.",
                "<p><strong>A new device</strong> signed in to your account.</p>");
        message = new UrgencyPrefixDecorator(message);
        message = new ConfidentialityFooterDecorator(message);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("template", "security-alert");
        metadata.put("priority", "high");

        return new NotificationRequest(
                "N-1003",
                "device-token-abc",
                NotificationType.PUSH,
                message,
                metadata);
    }

    private static void printSeparator() {
        System.out.println();
        System.out.println("-----");
        System.out.println();
    }
}
