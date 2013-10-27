package org.eminmamedov.smscenter.receivers.smpp.tasks;

import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.SMPPPacket;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;

/**
 * Task sends EnquireLink packages to check that connection still alive.
 * 
 * @author Emin Mamedov
 * 
 */
public class EnquireLinkSenderTask extends SenderTask {

    private static final Logger log = Logger.getLogger(EnquireLinkSenderTask.class);

    private static final int ENQUIRE_LINK_MAX_COUNT = 3;

    @Override
    public void executeSenderForHandler(SmppConnectionHandler handler) throws BadCommandIDException, IOException {
        log.debug("Executing tasks for handler " + handler);
        handler.incEnquireLinkCount();
        if (handler.getEnquireLinkCount() > ENQUIRE_LINK_MAX_COUNT) {
            log.info("Didn't receive enquire_links from " + handler.getUser());
            getSmppConnectionPool().close(handler);
            return;
        }
        if (handler.isNotBound()) {
            log.debug("Skip sending EnquireLink because handler is not in bound state");
            return;
        }
        EnquireLink req = (EnquireLink) getSmppReceiverHelper().newInstance(SMPPPacket.ENQUIRE_LINK);
        handler.sendRequest(req);
    }

}
