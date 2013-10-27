package org.eminmamedov.smscenter.receivers.smpp.processors;

import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.Unbind;
import ie.omk.smpp.message.UnbindResp;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppRequestProcessor;

public class UnbindProcessor extends SmppRequestProcessor {

    private static final Logger log = Logger.getLogger(UnbindProcessor.class);

    @Override
    public void processRequest(SmppConnectionHandler handler, SMPPPacket request) throws IOException {
        log.info("User " + handler.getUser().getName() + " unbinding from server");
        UnbindResp unbindResp = new UnbindResp(convertRequest(request));
        handler.sendResponse(unbindResp);
        getSmppConnectionPool().close(handler);
    }

    private Unbind convertRequest(SMPPPacket request) {
        if (request instanceof Unbind) {
            return (Unbind) request;
        }
        throw new IllegalArgumentException("Unsupported request type");
    }

}
