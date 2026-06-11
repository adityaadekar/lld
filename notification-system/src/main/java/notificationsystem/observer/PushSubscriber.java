package notificationsystem.observer;

import notificationsystem.channel.PushChannel;

public class PushSubscriber extends ChannelSubscriber {
    public PushSubscriber() {
        super(new PushChannel());
    }
}
