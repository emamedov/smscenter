package org.eminmamedov.smscenter.senders.smpp;

import ie.omk.smpp.message.SMPPPacket;

import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.springframework.stereotype.Component;

@Component
public class MessageStatusConverter {

    public MessageStatus convertToMessageStatus(String messageState) {
        MessageStatus messageStatus = null;
        if ("DELIVRD".equals(messageState)) {
            messageStatus = MessageStatus.MESSAGE_WAS_DELIVERED;
        } else if (("UNDELIV".equals(messageState)) || ("EXPIRED".equals(messageState))) {
            messageStatus = MessageStatus.MESSAGE_WASNT_DELIVERED;
        }
        return messageStatus;
    }

    public MessageStatus convertToMessageStatus(int state) {
        MessageStatus messageStatus = null;
        switch (state) {
        case SMPPPacket.SM_STATE_DELIVERED:
            messageStatus = MessageStatus.MESSAGE_WAS_DELIVERED;
            break;
        case SMPPPacket.SM_STATE_UNDELIVERABLE:
        case SMPPPacket.SM_STATE_EXPIRED:
            messageStatus = MessageStatus.MESSAGE_WASNT_DELIVERED;
            break;
        default:
            messageStatus = null;
            break;
        }
        return messageStatus;
    }

}
