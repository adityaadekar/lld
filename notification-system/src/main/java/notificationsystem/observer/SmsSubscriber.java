package notificationsystem.observer;

import notificationsystem.channel.DeliveryChannel;
import notificationsystem.channel.SmsChannel;

public class SmsSubscriber implements ChannelSubscriber {
    private final DeliveryChannel deliveryChannel = new SmsChannel();

    @Override
    public DeliveryChannel deliveryChannel() {
        return deliveryChannel;
    }
}
