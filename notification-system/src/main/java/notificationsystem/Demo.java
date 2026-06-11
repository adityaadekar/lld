package notificationsystem;

import notificationsystem.decorator.MessageDecorations;
import notificationsystem.model.NotificationMessage;
import notificationsystem.model.NotificationMessages;
import notificationsystem.service.NotificationService;

public final class Demo {
    private Demo() {
    }

    public static void main(String[] args) {
        NotificationService notifications = NotificationService.createDefault();

        sendSignedEmail(notifications);
        printSeparator();

        sendPlainSms(notifications);
        printSeparator();

        sendUrgentPush(notifications);
    }

    private static void sendSignedEmail(NotificationService notifications) {
        NotificationMessage message = NotificationMessages.html(
                "Invoice generated",
                "Your invoice for this billing cycle is ready.",
                "<p>Your invoice for this billing cycle is <strong>ready</strong>.</p>");

        notifications.sendEmail(
                "customer@example.com",
                message,
                MessageDecorations.signature("Finance Team"),
                MessageDecorations.tracking("INV-2026-001"));
    }

    private static void sendPlainSms(NotificationService notifications) {
        NotificationMessage message = NotificationMessages.plain(
                "OTP",
                "Your one-time password is 482913.");

        notifications.sendSms(
                "+15550101010",
                message);
    }

    private static void sendUrgentPush(NotificationService notifications) {
        NotificationMessage message = NotificationMessages.html(
                "Security alert",
                "A new device signed in to your account.",
                "<p><strong>A new device</strong> signed in to your account.</p>");

        notifications.sendPush(
                "device-token-abc",
                message,
                MessageDecorations.urgent(),
                MessageDecorations.confidentialityFooter());
    }

    private static void printSeparator() {
        System.out.println();
        System.out.println("-----");
        System.out.println();
    }
}
