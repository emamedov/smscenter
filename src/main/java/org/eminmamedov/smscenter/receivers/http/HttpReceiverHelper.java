package org.eminmamedov.smscenter.receivers.http;

import static org.eminmamedov.smscenter.receivers.http.HttpReceiverErrorCodes.ERR_INV_DST_ADR;
import static org.eminmamedov.smscenter.receivers.http.HttpReceiverErrorCodes.ERR_INV_SRC_ADR;
import static org.eminmamedov.smscenter.receivers.http.HttpReceiverErrorCodes.ERR_INV_TEXT_LENGTH;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.common.SmsCenterUtils;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class HttpReceiverHelper {

    private static final Logger log = Logger.getLogger(HttpReceiverHelper.class);

    @Autowired
    private SmsCenterService smsCenterService;
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private SmsCenterUtils smsCenterUtils;

    public List<Message> checkMessages(User user, List<Message> checkMessages) {
        log.debug("Check statuses of " + checkMessages.size() + " messages for user " + user);
        List<Message> checkedMessages = new ArrayList<Message>();
        List<Long> ids = new ArrayList<Long>();
        for (Message message : checkMessages) {
            ids.add(message.getServerId());
        }
        List<SMSMessage> result = smsCenterService.getMessages(ids);
        if (!CollectionUtils.isEmpty(result)) {
            for (SMSMessage smsMessage : result) {
                checkedMessages.add(messageConverter.createCheckMessageResponse(smsMessage));
            }
        }
        return checkedMessages;
    }

    public List<Message> sendMessages(User user, List<Message> sendMessages) {
        log.debug("Prepare to send " + sendMessages.size() + " messages that were received from user " + user);
        List<Message> sentMessages = new ArrayList<Message>();
        List<SMSMessage> preparedMessages = new ArrayList<SMSMessage>();
        for (Message message : sendMessages) {
            if (!isValidMessage(message, user)) {
                sentMessages.add(message);
                continue;
            }
            Integer clientId = message.getClientId();
            List<String> messageParts = smsCenterUtils.splitMessage(message.getText().trim());
            String senderSign = message.getSender();
            Sender sender = smsCenterService.getSender(user, senderSign);
            senderSign = senderSign != null ? senderSign : sender.getSign();
            for (int i = 0; i < messageParts.size(); i++) {
                int groupIndex = messageParts.size() == 1 ? 0 : i + 1;
                int groupCount = messageParts.size() == 1 ? 0 : messageParts.size();
                int groupId = groupCount > 0 ? 1 : 0;
                Long reservedId = smsCenterService.reserveServerId();
                String messagePart = messageParts.get(i);
                SMSMessage smsMessage = new SMSMessage(reservedId, clientId, user, sender, senderSign,
                        message.getReceiver(), messagePart, groupId, groupCount, groupIndex);
                preparedMessages.add(smsMessage);
                sentMessages.add(messageConverter.createSendMessageResponse(smsMessage));
            }
        }
        if (!CollectionUtils.isEmpty(preparedMessages)) {
            log.info("Try to send " + preparedMessages.size() + " messages by user " + user.getName());
            smsCenterService.addMessages(preparedMessages);
        }
        return sentMessages;
    }

    private boolean isValidMessage(Message message, User user) {
        Integer clientId = message.getClientId();
        String text = message.getText();
        if (StringUtils.isBlank(text)) {
            log.warn("Empty text detected in SMS with clientId " + clientId + " from user " + user);
            message.setErrorCode(ERR_INV_TEXT_LENGTH);
            return false;
        }
        String phone = smsCenterUtils.checkNumber(message.getReceiver());
        if (StringUtils.isBlank(phone)) {
            log.warn("Wrong receiver number detected in SMS with clientId " + clientId + " from user " + user);
            message.setErrorCode(ERR_INV_DST_ADR);
            return false;
        }
        String senderSign = message.getSender();
        Sender sender = smsCenterService.getSender(user, senderSign);
        if (sender == null) {
            log.warn("Wrong sender has been specified in SMS with clientId " + clientId + " from user " + user);
            message.setErrorCode(ERR_INV_SRC_ADR);
            return false;
        }
        message.setReceiver(phone);
        return true;
    }

    public void setSmsCenterService(SmsCenterService smsCenterService) {
        this.smsCenterService = smsCenterService;
    }

    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public void setSmsCenterUtils(SmsCenterUtils smsCenterUtils) {
        this.smsCenterUtils = smsCenterUtils;
    }

}
