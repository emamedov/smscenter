package org.eminmamedov.smscenter.senders;

import org.eminmamedov.smscenter.datamodel.Channel;

public interface SmsSenderFactory {

    SmsSender createSmsSender(Channel channel);

    boolean isApplicable(Channel channel);

}
