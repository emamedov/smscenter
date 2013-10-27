package org.eminmamedov.smscenter.senders.smpp;

import ie.omk.smpp.Address;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.Connection;
import ie.omk.smpp.SMPPException;
import ie.omk.smpp.SMPPRuntimeException;
import ie.omk.smpp.message.QuerySM;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.tlv.TLVTable;
import ie.omk.smpp.message.tlv.Tag;
import ie.omk.smpp.version.SMPPVersion;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.common.SmsCenterUtils;
import org.eminmamedov.smscenter.common.WorkerInitializer;
import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.exceptions.SmsCenterException;
import org.eminmamedov.smscenter.senders.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

// TODO Methods send and check throw SmsCenterException that is not catched
public class SmppSmsSender implements SmsSender {

    private static final Logger log = Logger.getLogger(SmppSmsSender.class);
    private static final int QUERY_PERIOD = Long.valueOf(DateUtils.MILLIS_PER_HOUR / 2).intValue();

    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private SmsCenterUtils smsCenterUtils;
    @Autowired
    private WorkerInitializer workerInitializer;
    private Connection smppConnection;
    private Channel channel;

    public SmppSmsSender(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void connect() {
        if (!getChannel().isEnabled()) {
            log.info("Skip connecting via channel because channel is disabled: " + getChannel());
            return;
        }
        if (isConnected()) {
            log.info("Skip connecting via channel because sender is connected already: " + getChannel());
            return;
        }
        log.info("Connecting to SMPP server via channel: " + getChannel());
        try {
            smppConnection = new Connection(getChannel().getHost(), getChannel().getPort(), true);
            SmppEventListener listener = new SmppEventListener(channel);
            workerInitializer.autowire(listener);
            smppConnection.addObserver(listener);
            smppConnection.autoAckLink(true);
            smppConnection.autoAckMessages(true);
            smppConnection.setVersion(SMPPVersion.V34);
            smppConnection.bind(Connection.TRANSCEIVER, getChannel().getLogin(), getChannel().getPassword(),
                    getChannel().getBindType());
        } catch (IOException e) {
            log.error(e, e);
            smppConnection = null;
        }
    }

    @Override
    public void closeConnection() {
        if (isConnected()) {
            log.info("Close SMPP connection for channel " + getChannel());
            try {
                if (smppConnection.isBound()) {
                    smppConnection.unbind();
                }
                smppConnection.force_unbind();
            } catch (IOException e) {
                log.error(e, e);
            }
            log.info("Connection with SMPP server has been closed successfully");
        }
        this.smppConnection = null;
    }

    @Override
    public void sendMessages() {
        if (!isBound()) {
            log.debug("Connection is not bound yet. Skip send operation for channel " + getChannel());
            return;
        }
        log.debug("Get messages with state " + MessageStatus.NEW_MESSAGE_STATE + " to send via channel " + getChannel());
        List<SMSMessage> messages = smsMapper.getMessagesByState(getChannel(), MessageStatus.NEW_MESSAGE_STATE,
                getChannel().getSendSpeed(), new Date());
        reserveMessages(messages);
        log.debug("Get messages with state " + MessageStatus.MESSAGE_RESERVED_BY_CHANNEL + " to send via channel "
                + getChannel());
        messages = smsMapper.getMessagesByState(getChannel(), MessageStatus.MESSAGE_RESERVED_BY_CHANNEL, getChannel()
                .getSendSpeed(), new Date());
        if (CollectionUtils.isEmpty(messages)) {
            log.debug("No messages have been found. Skip sending");
            return;
        }
        try {
            for (SMSMessage message : messages) {
                String phone = smsCenterUtils.checkNumber(message.getPhone());
                if (StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(message.getText())) {
                    message.setPhone(phone);
                    sendSubmitSM(message);
                    message.setStatus(MessageStatus.MESSAGE_IN_QUEUE_WASNT_SUBMIT);
                } else {
                    log.debug("ERROR IN MESSAGE: Message will be not sent. Please check phone and text. Message = "
                            + message);
                    message.setStatus(MessageStatus.MESSAGE_WASNT_DELIVERED);
                }
                smsMapper.update(message);
            }
        } catch (SMPPRuntimeException e) {
            log.error(e, e);
            throw new SmsCenterException("Error occured. Cant send submit_sm package", e);
        } catch (SMPPException e) {
            log.error(e, e);
            throw new SmsCenterException("Error occured. Cant send submit_sm package", e);
        } catch (IOException e) {
            log.error(e, e);
            throw new SmsCenterException("Error occured. Cant send submit_sm package", e);
        }
    }

    private void sendSubmitSM(SMSMessage message) throws BadCommandIDException, IOException {
        SubmitSM sm = (SubmitSM) smppConnection.newInstance(SMPPPacket.SUBMIT_SM);
        if (message.getSenderSign() != null) {
            sm.setSource(new Address(getChannel().getSourceAddrTON(), getChannel().getSourceAddrNPI(), message
                    .getSenderSign()));
        } else {
            sm.setSource(new Address(getChannel().getSourceAddrTON(), getChannel().getSourceAddrNPI(), message
                    .getSender().getSign()));
        }
        sm.setDestination(new Address(getChannel().getDestAddrTON(), getChannel().getDestAddrNPI(), message.getPhone()));
        sm.setMessageEncoding(smsCenterUtils.getTextEncoding(message.getText()));
        sm.setMessageText(message.getText());
        sm.setRegistered(1);
        sm.setSequenceNum(message.getId().intValue());
        if (message.getSmsGroupCount() > 0) {
            TLVTable tlv = new TLVTable();
            tlv.set(Tag.SAR_TOTAL_SEGMENTS, Integer.valueOf(message.getSmsGroupCount()));
            tlv.set(Tag.SAR_SEGMENT_SEQNUM, Integer.valueOf(message.getSmsGroupIndex()));
            tlv.set(Tag.SAR_MSG_REF_NUM, Integer.valueOf(message.getSmsGroupId()));
            sm.setTLVTable(tlv);
        }
        log.debug("Send message " + message + " via channel " + getChannel());
        smppConnection.sendRequest(sm);
    }

    private void reserveMessages(List<SMSMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        log.debug(messages.size() + " messages prepared to send via SMPP protocol. Trying to reserve it for channel "
                + getChannel());
        for (SMSMessage message : messages) {
            message.setStatus(MessageStatus.MESSAGE_RESERVED_BY_CHANNEL);
            smsMapper.update(message);
        }
    }

    @Override
    public void checkMessages() {
        if (!isBound()) {
            log.debug("Connection is not bound yet. Skip check operation for channel " + getChannel());
            return;
        }
        log.debug("Get messages with state " + MessageStatus.MESSAGE_IN_QUEUE + " to check via channel " + getChannel());
        Date lastUpdated = DateUtils.addMilliseconds(new Date(), -QUERY_PERIOD);
        List<SMSMessage> messages = smsMapper.getMessagesByState(getChannel(), MessageStatus.MESSAGE_IN_QUEUE,
                getChannel().getCheckSpeed(), lastUpdated);
        if (CollectionUtils.isEmpty(messages)) {
            log.debug("No messages have been found. Skip checking");
            return;
        }
        try {
            for (SMSMessage message : messages) {
                if (message.getSmscId() != null) {
                    sendQuerySM(message);
                    smsMapper.update(message);
                }
            }
        } catch (SMPPRuntimeException e) {
            log.error(e, e);
            throw new SmsCenterException("Error occured. Cant send query_sm package", e);
        } catch (SMPPException e) {
            log.error(e, e);
            throw new SmsCenterException("Error occured. Cant send query_sm package", e);
        } catch (IOException e) {
            log.error(e, e);
            throw new SmsCenterException("Error occured. Cant send query_sm package", e);
        }
    }

    private void sendQuerySM(SMSMessage message) throws BadCommandIDException, IOException {
        QuerySM qr = (QuerySM) smppConnection.newInstance(SMPPPacket.QUERY_SM);
        if (message.getSenderSign() != null) {
            qr.setSource(new Address(getChannel().getSourceAddrTON(), getChannel().getSourceAddrNPI(), message
                    .getSenderSign()));
        } else {
            qr.setSource(new Address(getChannel().getSourceAddrTON(), getChannel().getSourceAddrNPI(), message
                    .getSender().getSign()));
        }
        qr.setRegistered(1);
        qr.setSequenceNum(message.getId().intValue());
        qr.setMessageId(message.getSmscId());
        log.debug("Check message state for message " + message + " via channel " + getChannel());
        smppConnection.sendRequest(qr);
    }

    @Override
    public void sendPingPackage() {
        if (!isBound()) {
            log.debug("Connection is not bound yet. Skip ping operation for channel " + getChannel());
            return;
        }
        try {
            smppConnection.enquireLink();
        } catch (SMPPRuntimeException e) {
            log.error(e, e);
        } catch (IOException e) {
            log.error(e, e);
        }
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    public boolean isConnected() {
        return smppConnection != null;
    }

    public boolean isBound() {
        return (isConnected()) && (this.smppConnection.isBound());
    }

}
