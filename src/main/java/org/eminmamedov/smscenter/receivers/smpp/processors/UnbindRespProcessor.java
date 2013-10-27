package org.eminmamedov.smscenter.receivers.smpp.processors;

import ie.omk.smpp.message.SMPPPacket;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppRequestProcessor;

public class UnbindRespProcessor extends SmppRequestProcessor {

    private static final Logger log = Logger.getLogger(UnbindRespProcessor.class);

    @Override
    public void processRequest(SmppConnectionHandler handler, SMPPPacket request) {
        log.info("User has been successfully logged out: " + handler.getUser().getName());
        getSmppConnectionPool().close(handler);
    }

}
