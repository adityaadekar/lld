package notificationsystem.decorator;

public final class MessageDecorations {
    private MessageDecorations() {
    }

    public static MessageDecoration signature(String signerName) {
        return message -> new SignatureDecorator(message, signerName);
    }

    public static MessageDecoration tracking(String trackingId) {
        return message -> new TrackingFooterDecorator(message, trackingId);
    }

    public static MessageDecoration confidentialityFooter() {
        return ConfidentialityFooterDecorator::new;
    }

    public static MessageDecoration urgent() {
        return UrgencyPrefixDecorator::new;
    }
}
