package notificationsystem.observer;

import notificationsystem.channel.SmsChannel;

public class SmsSubscriber extends ChannelSubscriber {
    public SmsSubscriber() {
        super(new SmsChannel());
    }
}
