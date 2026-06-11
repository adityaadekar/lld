package notificationsystem.observer;

import java.util.Objects;
import notificationsystem.channel.DeliveryChannel;
import notificationsystem.channel.DeliveryReceipt;
import notificationsystem.model.NotificationRequest;
import notificationsystem.model.NotificationType;

public abstract class ChannelSubscriber implements NotificationObserver {
    private final DeliveryChannel deliveryChannel;

    protected ChannelSubscriber(DeliveryChannel deliveryChannel) {
        this.deliveryChannel = Objects.requireNonNull(deliveryChannel, "deliveryChannel cannot be null");
    }

    @Override
    public void update(NotificationRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        if (request.type() != supportedType()) {
            return;
        }
        DeliveryReceipt receipt = deliveryChannel.deliver(request);
        afterDelivery(receipt);
    }

    protected NotificationType supportedType() {
        return deliveryChannel.type();
    }

    protected void afterDelivery(DeliveryReceipt receipt) {
        System.out.println(receipt);
    }
}
