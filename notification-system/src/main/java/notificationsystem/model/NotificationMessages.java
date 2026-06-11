package notificationsystem.model;

public final class NotificationMessages {
    private NotificationMessages() {
    }

    public static NotificationMessage plain(String subject, String body) {
        return new PlainNotificationMessage(subject, body);
    }

    public static NotificationMessage html(String subject, String body, String htmlBody) {
        return new HtmlNotificationMessage(subject, body, htmlBody);
    }
}
