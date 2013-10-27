package org.eminmamedov.smscenter.senders.smpp;

import ie.omk.smpp.Connection;
import ie.omk.smpp.SMPPException;
import ie.omk.smpp.event.ReceiverExceptionEvent;
import ie.omk.smpp.event.ReceiverExitEvent;
import ie.omk.smpp.event.SMPPEventAdapter;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPResponse;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.message.Unbind;
import ie.omk.smpp.message.UnbindResp;
import ie.omk.smpp.message.tlv.Tag;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.senders.SmsSendersManager;
import org.springframework.beans.factory.annotation.Autowired;

public class SmppEventListener extends SMPPEventAdapter {

    private static final Logger log = Logger.getLogger(SmppEventListener.class);
    private static final int TROTTLING_ERROR = 0x58;
    private static final long DELIVER_WAIT_PERIOD = DateUtils.MILLIS_PER_HOUR;
    private static final Pattern SMPPPACKET_PATTERN = Pattern.compile("id:([0-9]*).*stat:([A-Z]*)");

    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private MessageStatusConverter messageStatusConverter;
    @Autowired
    private SmsSendersManager smsSendersManager;
    private Channel channel;

    public SmppEventListener(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void receiverExit(Connection source, ReceiverExitEvent rev) {
        log.error("receiverExit", rev.getException());
        smsSendersManager.closeSender(channel);
    }

    @Override
    public void receiverExitException(Connection source, ReceiverExitEvent rev) {
        log.error("receiverExitException", rev.getException());
        smsSendersManager.closeSender(channel);
    }

    @Override
    public void receiverException(Connection source, ReceiverExceptionEvent rev) {
        log.error("receiverException", rev.getException());
    }

    @Override
    public void deliverSM(Connection source, DeliverSM dm) {
        log.debug("DeliverSM has been received:\n" + SmppPacketUtils.toString(dm));
        String messageText = dm.getMessageText();
        if (StringUtils.isBlank(messageText)) {
            return;
        }
        String smscId = "";
        Object stateObj = dm.getOptionalParameter(Tag.MESSAGE_STATE);
        Object smscIdObj = dm.getOptionalParameter(Tag.RECEIPTED_MESSAGE_ID);
        MessageStatus messageStatus = null;
        if ((stateObj == null) || (smscIdObj == null)) {
            Matcher textMatcher = SMPPPACKET_PATTERN.matcher(messageText);
            if (textMatcher.matches()) {
                smscId = textMatcher.group(1);
                messageStatus = messageStatusConverter.convertToMessageStatus(textMatcher.group(2));
            }
        } else {
            smscId = (String) smscIdObj;
            messageStatus = messageStatusConverter.convertToMessageStatus((Integer) stateObj);
        }
        if (messageStatus != null) {
            smscId = StringUtils.stripStart(smscId, "0");
            SMSMessage message = smsMapper.getMessageBySmscId(channel, smscId);
            if (message != null) {
                message.setStatus(messageStatus);
                smsMapper.update(message);
            }
        } else {
            log.warn("Wrong format of DELIVER_SM packet");
        }
    }

    @Override
    public void bindResponse(Connection source, BindResp br) {
        log.debug("BindResponse has been received:\n" + SmppPacketUtils.toString(br));
        if (br.getCommandStatus() != 0) {
            log.info("Couldn't connect to the SMSC by channel " + channel);
            smsSendersManager.closeSender(channel);
        } else {
            log.info("SMPP connection has been successfully initialized by channel " + channel);
        }
    }

    @Override
    public void unbind(Connection source, Unbind ubd) {
        log.debug("Unbind has been received:\n " + SmppPacketUtils.toString(ubd));
        try {
            source.unbind(new UnbindResp(ubd));
        } catch (RuntimeException e) {
            log.error(e, e);
        } catch (IOException e) {
            log.error(e, e);
        } catch (SMPPException e) {
            log.error(e, e);
        }
        smsSendersManager.closeSender(channel);
    }

    @Override
    public void unbindResponse(Connection source, UnbindResp ubr) {
        log.debug("UnbindResponse has been received:\n" + SmppPacketUtils.toString(ubr));
        smsSendersManager.closeSender(channel);
    }

    @Override
    public void unidentified(Connection source, SMPPPacket pak) {
        log.debug("Unknown packet has been received:\n" + SmppPacketUtils.toString(pak));
    }

    @Override
    public void submitSMResponse(Connection source, SubmitSMResp smr) {
        log.debug("SubmitSMResponse has been received:\n" + SmppPacketUtils.toString(smr));
        int serverId = smr.getSequenceNum();
        String messageId = smr.getMessageId();
        MessageStatus status = null;
        if (StringUtils.isNotBlank(messageId)) {
            status = MessageStatus.MESSAGE_IN_QUEUE;
        } else if (smr.getCommandStatus() == TROTTLING_ERROR) {
            status = MessageStatus.NEW_MESSAGE_STATE;
        } else if (serverId > 0) {
            status = MessageStatus.MESSAGE_WASNT_DELIVERED;
        }
        SMSMessage message = smsMapper.getMessage(Long.valueOf(serverId));
        if (message != null) {
            message.setSmscId(messageId);
            message.setStatus(status);
            smsMapper.update(message);
        } else {
            log.warn("Can't process SubmitSMResp. Message wasn't found");
        }
    }

    @Override
    public void queryResponse(Connection source, SMPPResponse qr) {
        log.debug("QueryResponse has been received:\n" + SmppPacketUtils.toString(qr));
        String messageId = qr.getMessageId();
        if (StringUtils.isBlank(messageId)) {
            return;
        }
        SMSMessage message = smsMapper.getMessageBySmscId(channel, messageId);
        if (message == null) {
            return;
        }
        switch (qr.getMessageStatus()) {
        case SMPPResponse.SM_STATE_DELIVERED:
            message.setStatus(MessageStatus.MESSAGE_WAS_DELIVERED);
            break;
        case SMPPResponse.SM_STATE_UNDELIVERABLE:
            message.setStatus(MessageStatus.MESSAGE_WASNT_DELIVERED);
            break;
        case SMPPResponse.SM_STATE_EN_ROUTE:
            Date finalDate = null;
            if (qr.getFinalDate() == null) {
                finalDate = message.getSendDate();
            } else {
                finalDate = qr.getFinalDate().getCalendar().getTime();
            }
            long seconds = new Date().getTime() - finalDate.getTime();
            if (seconds > DELIVER_WAIT_PERIOD) {
                log.info("Message was sent more than " + (seconds / DateUtils.MILLIS_PER_MINUTE)
                        + " minutes ago. Set message status to " + MessageStatus.MESSAGE_WASNT_DELIVERED);
                message.setStatus(MessageStatus.MESSAGE_WASNT_DELIVERED);
            }
            break;
        default:
            log.info("Unknown state " + qr.getMessageStatus() + ". Set message status to "
                    + MessageStatus.MESSAGE_WASNT_DELIVERED);
            message.setStatus(MessageStatus.MESSAGE_WASNT_DELIVERED);
            break;
        }
        smsMapper.update(message);
    }

    public Channel getChannel() {
        return channel;
    }

}
