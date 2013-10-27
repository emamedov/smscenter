package org.eminmamedov.smscenter.receivers.smpp.processors;

import static org.eminmamedov.smscenter.receivers.smpp.SmppReceiverErrorCodes.*;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.message.tlv.Tag;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.common.SmsCenterUtils;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppRequestProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class SubmitSmProcessor extends SmppRequestProcessor {

    private static final String ERROR_IN_SUBMIT_PACKAGE = "Error in SubmitSM package that received from user ";
    private static final Logger log = Logger.getLogger(SubmitSmProcessor.class);

    @Autowired
    private SmsCenterUtils smsCenterUtils;

    @Override
    public void processRequest(SmppConnectionHandler handler, SMPPPacket request) throws IOException {
        SubmitSM submitSmRequest = convertRequest(request);
        SubmitSMResp submitResp = new SubmitSMResp(submitSmRequest);
        User user = handler.getUser();
        if (handler.isNotBound()) {
            log.warn("User " + user + " tried to send message via channel not in BOUND state");
            submitResp.setCommandStatus(ESME_RINVBNDSTS);
        } else {
            processSubmitSm(submitSmRequest, submitResp, user);
        }
        handler.resetEnquireLinkCount();
        handler.sendResponse(submitResp);
    }

    private void processSubmitSm(SubmitSM submitSmRequest, SubmitSMResp submitResp, User user) {
        int seqNum = submitSmRequest.getSequenceNum();
        if (seqNum > 0) {
            String phone = submitSmRequest.getDestination().getAddress();
            phone = smsCenterUtils.checkNumber(phone);
            if (phone != null) {
                String senderSign = submitSmRequest.getSource().getAddress();
                Sender sender = getSmsCenterService().getSender(user, senderSign);
                if (sender != null) {
                    senderSign = StringUtils.isNotBlank(senderSign) ? senderSign : sender.getSign();
                    addMessage(submitSmRequest, submitResp, user, seqNum, phone, senderSign, sender);
                } else {
                    log.warn(ERROR_IN_SUBMIT_PACKAGE + user + ". Sender has not been found.");
                    submitResp.setCommandStatus(ESME_RINVSRCADR);
                }
            } else {
                log.warn(ERROR_IN_SUBMIT_PACKAGE + user + ". Error in receiver's phone number.");
                submitResp.setCommandStatus(ESME_RINVDSTADR);
            }
        } else {
            log.warn(ERROR_IN_SUBMIT_PACKAGE + user + ". Wrong sequence_number.");
            submitResp.setCommandStatus(ESME_RINVMSGID);
        }
    }

    private void addMessage(SubmitSM submitSmRequest, SubmitSMResp submitResp, User user, int clientId, String phone,
            String senderSign, Sender sender) {
        Object obj = submitSmRequest.getOptionalParameter(Tag.SAR_MSG_REF_NUM);
        int groupIndex = 0;
        int groupSegmentsCount = 0;
        int groupSegmentIndex = 0;
        if (obj != null) {
            groupIndex = (Integer) obj;
            groupSegmentsCount = (Integer) submitSmRequest.getOptionalParameter(Tag.SAR_TOTAL_SEGMENTS);
            groupSegmentIndex = (Integer) submitSmRequest.getOptionalParameter(Tag.SAR_SEGMENT_SEQNUM);
        }
        if (validateGroupParams(groupIndex, groupSegmentsCount, groupSegmentIndex)) {
            if (groupIndex == 0) {
                groupSegmentIndex = 0;
                groupSegmentsCount = 0;
            }
            SMSMessage message = new SMSMessage(getSmsCenterService().reserveServerId(), clientId, user, sender,
                    senderSign, phone, submitSmRequest.getMessageText(), groupIndex, groupSegmentsCount,
                    groupSegmentIndex);
            List<SMSMessage> messages = Collections.singletonList(message);
            try {
                log.debug("Inserting message " + message + " in database from " + user);
                getSmsCenterService().addMessages(messages);
                submitResp.setMessageId(String.valueOf(message.getId()));
            } catch (RuntimeException e) {
                log.error("Unknown error occured", e);
                submitResp.setCommandStatus(ESME_RSYSERR);
            }
        } else {
            log.warn(ERROR_IN_SUBMIT_PACKAGE + user + ". Error in optional parameters.");
            submitResp.setCommandStatus(ESME_RINVOPTPARAMVAL);
        }
    }

    private boolean validateGroupParams(int groupIndex, int groupSegmentsCount, int groupSegmentIndex) {
        return (groupIndex == 0) || ((groupSegmentIndex > 0) && (groupSegmentIndex <= groupSegmentsCount));
    }

    private SubmitSM convertRequest(SMPPPacket request) {
        if (request instanceof SubmitSM) {
            return (SubmitSM) request;
        }
        throw new IllegalArgumentException("Unsupported request type");
    }

    public void setSmsCenterUtils(SmsCenterUtils smsCenterUtils) {
        this.smsCenterUtils = smsCenterUtils;
    }

}
