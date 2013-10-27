package org.eminmamedov.smscenter.receivers;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;

public class SmsReceiversManager {

    @Resource(name = "smsReceivers")
    private List<SmsReceiver> smsReceivers;

    public void startAll() {
        if (CollectionUtils.isEmpty(smsReceivers)) {
            return;
        }
        for (SmsReceiver receiver : smsReceivers) {
            receiver.start();
        }
    }

    public void stopAll() {
        if (CollectionUtils.isEmpty(smsReceivers)) {
            return;
        }
        for (SmsReceiver receiver : smsReceivers) {
            receiver.stop();
        }
    }

    public void setSmsReceivers(List<SmsReceiver> smsReceivers) {
        this.smsReceivers = smsReceivers;
    }

}
