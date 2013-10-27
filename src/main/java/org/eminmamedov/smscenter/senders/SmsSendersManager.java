package org.eminmamedov.smscenter.senders;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.eminmamedov.smscenter.dao.ChannelMapper;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.springframework.beans.factory.annotation.Autowired;

public class SmsSendersManager {

    @Autowired
    private ChannelMapper channelMapper;
    @Resource(name = "smsSendersFactories")
    private List<SmsSenderFactory> smsSendersFactories;
    private List<SmsSender> smsSenders;

    @PostConstruct
    public synchronized void initSmsSenders() {
        if (smsSenders != null) {
            return;
        }
        smsSenders = new LinkedList<SmsSender>();
        List<Channel> channels = channelMapper.findAll();
        for (Channel channel : channels) {
            addSender(channel);
        }
    }

    public synchronized void sendMessages() {
        for (SmsSender sender : smsSenders) {
            sender.sendMessages();
        }
    }

    public synchronized void checkMessages() {
        for (SmsSender sender : smsSenders) {
            sender.checkMessages();
        }
    }

    public synchronized void sendPingPackage() {
        for (SmsSender sender : smsSenders) {
            sender.sendPingPackage();
        }
    }

    public synchronized void closeAllSenders() {
        for (SmsSender sender : smsSenders) {
            sender.closeConnection();
        }
        smsSenders.clear();
    }

    public synchronized void closeSender(Channel channel) {
        if (channel == null) {
            return;
        }
        SmsSender sender = getSenderByChannel(channel);
        if (sender != null) {
            sender.closeConnection();
            smsSenders.remove(sender);
        }
    }

    public synchronized void startSender(Channel channel) {
        if (channel == null) {
            return;
        }
        SmsSender sender = getSenderByChannel(channel);
        if (sender != null) {
            sender.connect();
        }
    }

    public synchronized void addSender(Channel channel) {
        if (channel.isEnabled()) {
            for (SmsSenderFactory factory : smsSendersFactories) {
                if (factory.isApplicable(channel)) {
                    smsSenders.add(factory.createSmsSender(channel));
                    return;
                }
            }
        }
    }

    public synchronized List<SmsSender> getSmsSenders() {
        return new ArrayList<SmsSender>(smsSenders);
    }

    private SmsSender getSenderByChannel(Channel channel) {
        for (SmsSender sender : smsSenders) {
            if (sender.getChannel().getName().equals(channel.getName())) {
                return sender;
            }
        }
        return null;
    }

}
