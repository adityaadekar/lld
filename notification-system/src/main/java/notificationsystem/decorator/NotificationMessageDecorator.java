package notificationsystem.decorator;

import java.util.Optional;
import notificationsystem.model.NotificationMessage;

public interface NotificationMessageDecorator extends NotificationMessage {
    NotificationMessage wrappedMessage();

    @Override
    default String subject() {
        return wrappedMessage().subject();
    }

    @Override
    default String body() {
        return wrappedMessage().body();
    }

    @Override
    default Optional<String> htmlBody() {
        return wrappedMessage().htmlBody();
    }
}
