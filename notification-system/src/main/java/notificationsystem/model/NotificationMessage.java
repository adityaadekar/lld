package notificationsystem.model;

import java.util.Optional;

public interface NotificationMessage {
    String subject();

    String body();

    default Optional<String> htmlBody() {
        return Optional.empty();
    }
}
