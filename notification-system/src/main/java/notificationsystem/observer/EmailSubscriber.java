package notificationsystem.observer;

import notificationsystem.channel.EmailChannel;

public class EmailSubscriber extends ChannelSubscriber {
    public EmailSubscriber() {
        super(new EmailChannel());
    }
}
