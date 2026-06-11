package notificationsystem.observer;

import notificationsystem.channel.DeliveryChannel;
import notificationsystem.channel.PushChannel;

public class PushSubscriber implements ChannelSubscriber {
    private final DeliveryChannel deliveryChannel = new PushChannel();

    @Override
    public DeliveryChannel deliveryChannel() {
        return deliveryChannel;
    }
}
