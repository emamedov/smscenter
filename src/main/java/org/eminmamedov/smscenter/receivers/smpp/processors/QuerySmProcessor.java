package org.eminmamedov.smscenter.receivers.smpp.processors;

import static ie.omk.smpp.message.SMPPPacket.*;
import static org.eminmamedov.smscenter.datamodel.MessageStatus.*;
import static org.eminmamedov.smscenter.receivers.smpp.SmppReceiverErrorCodes.ESME_RINVMSGID;
import ie.omk.smpp.message.QuerySM;
import ie.omk.smpp.message.QuerySMResp;
import ie.omk.smpp.message.SMPPPacket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppRequestProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class QuerySmProcessor extends SmppRequestProcessor {

    private static final Logger log = Logger.getLogger(QuerySmProcessor.class);

    private Map<MessageStatus, Integer> statusesMap;

    @Autowired
    private SmsMapper smsMapper;

    public QuerySmProcessor() {
        this.statusesMap = new HashMap<MessageStatus, Integer>();
        statusesMap.put(MESSAGE_WASNT_DELIVERED, SM_STATE_UNDELIVERABLE);
        statusesMap.put(NEW_MESSAGE_STATE, SM_STATE_EN_ROUTE);
        statusesMap.put(MESSAGE_IN_QUEUE, SM_STATE_EN_ROUTE);
        statusesMap.put(MESSAGE_WAS_DELIVERED, SM_STATE_DELIVERED);
        statusesMap.put(MESSAGE_IN_QUEUE_WASNT_SUBMIT, SM_STATE_EN_ROUTE);
        statusesMap.put(MESSAGE_RESERVED_BY_CHANNEL, SM_STATE_EN_ROUTE);
    }

    @Override
    public void processRequest(SmppConnectionHandler handler, SMPPPacket request) throws IOException {
        QuerySMResp resp = new QuerySMResp(convertRequest(request));
        String messageId = resp.getMessageId();
        if (!NumberUtils.isDigits(messageId)) {
            resp.setCommandStatus(ESME_RINVMSGID);
        } else {
            SMSMessage message = smsMapper.getMessage(Long.valueOf(messageId));
            if (message == null) {
                resp.setCommandStatus(ESME_RINVMSGID);
            } else {
                resp.setMessageStatus(statusesMap.get(message.getStatus()));
            }
        }
        handler.sendResponse(resp);
        log.info("query_sm_resp has been sent to user [" + handler.getUser().getName() + "]");
        handler.resetEnquireLinkCount();
    }

    private QuerySM convertRequest(SMPPPacket request) {
        if (request instanceof QuerySM) {
            return (QuerySM) request;
        }
        throw new IllegalArgumentException("Unsupported request type");
    }

}
