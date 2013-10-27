package org.eminmamedov.smscenter.senders;

import org.eminmamedov.smscenter.datamodel.Channel;

public interface SmsSender {

    void sendMessages();

    void checkMessages();

    void sendPingPackage();

    void connect();

    void closeConnection();

    Channel getChannel();

}
