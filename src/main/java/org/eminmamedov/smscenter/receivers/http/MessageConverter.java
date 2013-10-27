package org.eminmamedov.smscenter.receivers.http;

import org.eminmamedov.smscenter.common.DbValueUtils;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.springframework.stereotype.Component;

@Component
public class MessageConverter {

    public Message createSendMessageResponse(SMSMessage smsMessage) {
        Message message = new Message();
        copyCommonFields(smsMessage, message);
        message.setGroupIndex(smsMessage.getSmsGroupIndex());
        message.setGroupCount(smsMessage.getSmsGroupCount());
        message.setText(smsMessage.getText());
        return message;
    }

    public Message createCheckMessageResponse(SMSMessage smsMessage) {
        Message message = new Message();
        copyCommonFields(smsMessage, message);
        message.setStatus((Integer) DbValueUtils.getDbValue(smsMessage.getStatus()));
        return message;
    }

    private void copyCommonFields(SMSMessage source, Message target) {
        target.setServerId(source.getId());
        target.setClientId(source.getClientId());
    }

}
