package org.eminmamedov.smscenter.receivers.smpp;

import ie.omk.smpp.message.SMPPPacket;

import java.io.IOException;

import org.eminmamedov.smscenter.services.SmsCenterService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SmppRequestProcessor {

    @Autowired
    private SmsCenterService smsCenterService;
    @Autowired
    private SmppConnectionPool smppConnectionPool;

    public abstract void processRequest(SmppConnectionHandler handler, SMPPPacket request) throws IOException;

    public SmsCenterService getSmsCenterService() {
        return smsCenterService;
    }

    public void setSmsCenterService(SmsCenterService smsCenterService) {
        this.smsCenterService = smsCenterService;
    }

    public SmppConnectionPool getSmppConnectionPool() {
        return smppConnectionPool;
    }

    public void setSmppConnectionPool(SmppConnectionPool smppConnectionPool) {
        this.smppConnectionPool = smppConnectionPool;
    }

}
