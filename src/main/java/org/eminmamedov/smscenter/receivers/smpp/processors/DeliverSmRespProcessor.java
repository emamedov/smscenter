package org.eminmamedov.smscenter.receivers.smpp.processors;

import ie.omk.smpp.message.SMPPPacket;

import java.io.IOException;

import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppRequestProcessor;

public class DeliverSmRespProcessor extends SmppRequestProcessor {

    @Override
    public void processRequest(SmppConnectionHandler handler, SMPPPacket request) throws IOException {
        int serverId = request.getSequenceNum();
        getSmsCenterService().setInformedFlagForMessage(Long.valueOf(serverId), true);
        handler.resetEnquireLinkCount();
    }

}
