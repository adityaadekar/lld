package notificationsystem;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import notificationsystem.channel.EmailChannel;
import notificationsystem.channel.PushChannel;
import notificationsystem.decorator.SignatureDecorator;
import notificationsystem.decorator.TrackingFooterDecorator;
import notificationsystem.model.HtmlNotificationMessage;
import notificationsystem.model.NotificationMessage;
import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;

public final class NotificationSystemTest {
    private NotificationSystemTest() {
    }

    public static void main(String[] args) {
        decoratedHtmlMessageKeepsRichBody();
        emailChannelDeliversHtmlBody();
        pushChannelDeliversHtmlPayload();
        System.out.println("All notification system tests passed.");
    }

    private static void decoratedHtmlMessageKeepsRichBody() {
        NotificationMessage message = new HtmlNotificationMessage(
                "Deployment",
                "Deployment finished.",
                "<p>Deployment <strong>finished</strong>.</p>");
        message = new SignatureDecorator(message, "Release & Ops <Team>");
        message = new TrackingFooterDecorator(message, "DEPLOY<42>");

        String htmlBody = message.htmlBody().orElseThrow(AssertionError::new);

        assertContains(message.body(), "Release & Ops <Team>", "plain text signature");
        assertContains(htmlBody, "Release &amp; Ops &lt;Team&gt;", "escaped html signature");
        assertContains(htmlBody, "DEPLOY&lt;42&gt;", "escaped html tracking id");
    }

    private static void emailChannelDeliversHtmlBody() {
        NotificationRequest request = request(
                "EMAIL-1",
                "customer@example.com",
                NotificationType.EMAIL,
                "<p>Email <strong>body</strong>.</p>");

        String output = captureOutput(() -> new EmailChannel().deliver(request));

        assertContains(output, "Text body: Plain body.", "email text body");
        assertContains(output, "HTML body: <p>Email <strong>body</strong>.</p>", "email html body");
    }

    private static void pushChannelDeliversHtmlPayload() {
        NotificationRequest request = request(
                "PUSH-1",
                "device-token",
                NotificationType.PUSH,
                "<p>Push <strong>payload</strong>.</p>");

        String output = captureOutput(() -> new PushChannel().deliver(request));

        assertContains(output, "Text preview: Plain body.", "push text preview");
        assertContains(output, "HTML payload: <p>Push <strong>payload</strong>.</p>", "push html payload");
    }

    private static NotificationRequest request(
            String id,
            String recipient,
            NotificationType type,
            String htmlBody) {
        return new NotificationRequest(
                id,
                recipient,
                type,
                new HtmlNotificationMessage("Subject", "Plain body.", htmlBody),
                Collections.emptyMap());
    }

    private static String captureOutput(Runnable runnable) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output));
            runnable.run();
            return output.toString();
        } finally {
            System.setOut(originalOut);
        }
    }

    private static void assertContains(String actual, String expected, String description) {
        if (!actual.contains(expected)) {
            throw new AssertionError("Expected " + description + " to contain: " + expected + "\nActual: " + actual);
        }
    }
}
