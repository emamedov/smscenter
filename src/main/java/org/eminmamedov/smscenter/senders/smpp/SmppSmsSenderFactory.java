package org.eminmamedov.smscenter.senders.smpp;

import org.eminmamedov.smscenter.common.WorkerInitializer;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.senders.SmsSender;
import org.eminmamedov.smscenter.senders.SmsSenderFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SmppSmsSenderFactory implements SmsSenderFactory {

    @Autowired
    private WorkerInitializer workerInitializer;

    @Override
    public SmsSender createSmsSender(Channel channel) {
        SmsSender sender = new SmppSmsSender(channel);
        workerInitializer.autowire(sender);
        return sender;
    }

    @Override
    public boolean isApplicable(Channel channel) {
        return true;
    }

}
