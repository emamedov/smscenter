package org.eminmamedov.smscenter.receivers.smpp.tasks;

import ie.omk.smpp.BadCommandIDException;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionPool;
import org.eminmamedov.smscenter.receivers.smpp.SmppReceiverHelper;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base abstract class for all senders tasks
 * 
 * @author Emin Mamedov
 * 
 */
public abstract class SenderTask {

    private static final Logger log = Logger.getLogger(DeliverSMSenderTask.class);

    @Autowired
    private SmppReceiverHelper smppReceiverHelper;
    @Autowired
    private SmppConnectionPool smppConnectionPool;
    @Autowired
    private SmsCenterService smsCenterService;

    public void sendRequests() {
        log.debug(this.getClass().getSimpleName() + ": Going to send packets");
        for (SmppConnectionHandler handler : getSmppConnectionPool().getConnectionHandlers()) {
            try {
                executeSenderForHandler(handler);
            } catch (BadCommandIDException e) {
                log.error("Unknown command ID", e);
                getSmppConnectionPool().close(handler);
            } catch (IOException e) {
                log.warn(e, e);
                getSmppConnectionPool().close(handler);
            } catch (RuntimeException e) {
                log.error("FATAL UNKNOWN ERROR! PLEASE CHECK LOGS!", e);
                getSmppConnectionPool().close(handler);
            }
        }
    }

    public abstract void executeSenderForHandler(SmppConnectionHandler handler) throws BadCommandIDException, IOException;

    public SmppReceiverHelper getSmppReceiverHelper() {
        return smppReceiverHelper;
    }

    public void setSmppReceiverHelper(SmppReceiverHelper smppReceiverHelper) {
        this.smppReceiverHelper = smppReceiverHelper;
    }

    public SmppConnectionPool getSmppConnectionPool() {
        return smppConnectionPool;
    }

    public void setSmppConnectionPool(SmppConnectionPool smppConnectionPool) {
        this.smppConnectionPool = smppConnectionPool;
    }

    public SmsCenterService getSmsCenterService() {
        return smsCenterService;
    }

    public void setSmsCenterService(SmsCenterService smsCenterService) {
        this.smsCenterService = smsCenterService;
    }

}
