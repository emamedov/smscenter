package org.eminmamedov.smscenter.receivers.smpp.processors;

import ie.omk.smpp.message.SMPPPacket;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppRequestProcessor;

public class EnquireLinkRespProcessor extends SmppRequestProcessor {

    private static final Logger log = Logger.getLogger(EnquireLinkRespProcessor.class);

    @Override
    public void processRequest(SmppConnectionHandler handler, SMPPPacket request) {
        log.info("enquire_link_resp has been received from user [" + handler.getUser().getName() + "]");
        handler.resetEnquireLinkCount();
    }

}
