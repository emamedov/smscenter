package org.eminmamedov.smscenter.receivers.smpp.processors;

import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.EnquireLinkResp;
import ie.omk.smpp.message.SMPPPacket;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppRequestProcessor;

public class EnquireLinkProcessor extends SmppRequestProcessor {

    private static final Logger log = Logger.getLogger(EnquireLinkProcessor.class);

    @Override
    public void processRequest(SmppConnectionHandler handler, SMPPPacket request) throws IOException {
        EnquireLinkResp resp = new EnquireLinkResp(convertRequest(request));
        handler.sendResponse(resp);
        log.info("enquire_link_resp has been sent to user [" + handler.getUser().getName() + "]");
        handler.resetEnquireLinkCount();
    }

    private EnquireLink convertRequest(SMPPPacket request) {
        if (request instanceof EnquireLink) {
            return (EnquireLink) request;
        }
        throw new IllegalArgumentException("Unsupported request type");
    }

}
