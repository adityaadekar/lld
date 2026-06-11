package notificationsystem.observer;

import notificationsystem.channel.DeliveryChannel;
import notificationsystem.channel.EmailChannel;

public class EmailSubscriber implements ChannelSubscriber {
    private final DeliveryChannel deliveryChannel = new EmailChannel();

    @Override
    public DeliveryChannel deliveryChannel() {
        return deliveryChannel;
    }
}
