package notificationsystem.observer;

import java.util.Objects;
import notificationsystem.channel.DeliveryChannel;
import notificationsystem.channel.DeliveryReceipt;
import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;

public interface ChannelSubscriber extends NotificationObserver {
    DeliveryChannel deliveryChannel();

    @Override
    default void update(NotificationRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        if (request.type() != supportedType()) {
            return;
        }
        DeliveryReceipt receipt = deliveryChannel().deliver(request);
        afterDelivery(receipt);
    }

    default NotificationType supportedType() {
        return deliveryChannel().type();
    }

    default void afterDelivery(DeliveryReceipt receipt) {
        System.out.println(receipt);
    }
}
