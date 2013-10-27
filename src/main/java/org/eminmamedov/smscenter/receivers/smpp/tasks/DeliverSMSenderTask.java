package org.eminmamedov.smscenter.receivers.smpp.tasks;

import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.tlv.Tag;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;

/**
 * DeliverSM sender task
 * 
 * @author Emin Mamedov
 * 
 */
public class DeliverSMSenderTask extends SenderTask {

    private static final Logger log = Logger.getLogger(DeliverSMSenderTask.class);

    private static final int DELIVER_SM_COUNT = 100;
    private static final int SMS_ID_LENGTH = 10;
    private static final int SMS_TEXT_LENGTH = 20;

    private DateFormat dateFormat = new SimpleDateFormat("yyMMddhhmm");

    @Override
    public void executeSenderForHandler(SmppConnectionHandler handler) throws BadCommandIDException, IOException {
        log.debug("Executing tasks for handler " + handler);
        if (handler.isNotBound()) {
            log.debug("Skip processing because handler is not in bound state");
            return;
        }
        List<SMSMessage> messages = getSmsCenterService().getUpdatedMessages(handler.getUser(), DELIVER_SM_COUNT);
        log.debug(messages.size() + " messages have been found with informed flag = 0");
        for (SMSMessage message : messages) {
            StringBuilder b = new StringBuilder("id:");
            String leftPadedId = StringUtils.leftPad(String.valueOf(message.getId()), SMS_ID_LENGTH, "0");
            b.append(leftPadedId);
            b.append(" sub:000 dlvrd:000 submit date:");
            b.append(dateFormat.format(message.getSendDate().getTime()));
            b.append(" done date:");
            b.append(dateFormat.format(message.getSendDate().getTime()));
            b.append(" stat:");
            switch (message.getStatus()) {
            case MESSAGE_WAS_DELIVERED:
                b.append("DELIVRD");
                break;
            default:
                b.append("UNDELIV");
                break;
            }
            b.append(" err:000");
            b.append(" Text:" + StringUtils.left(message.getText(), SMS_TEXT_LENGTH));
            DeliverSM delSM = (DeliverSM) getSmppReceiverHelper().newInstance(SMPPPacket.DELIVER_SM);
            delSM.setSequenceNum(message.getId().intValue());
            delSM.setMessageText(b.toString());
            delSM.setEsmClass(SMPPPacket.SMC_RECEIPT);
            delSM.setOptionalParameter(Tag.MESSAGE_STATE, message.getStatus().getDbValue());
            delSM.setOptionalParameter(Tag.RECEIPTED_MESSAGE_ID, String.valueOf(message.getId()));
            handler.sendRequest(delSM);
        }
    }

}
