package org.eminmamedov.smscenter.receivers.smpp.processors;

import static org.eminmamedov.smscenter.receivers.smpp.BoundState.*;
import static org.eminmamedov.smscenter.receivers.smpp.SmppReceiverErrorCodes.*;
import ie.omk.smpp.message.Bind;
import ie.omk.smpp.message.BindReceiverResp;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.BindTransceiverResp;
import ie.omk.smpp.message.BindTransmitterResp;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.tlv.Tag;
import ie.omk.smpp.version.SMPPVersion;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.exceptions.NotFoundException;
import org.eminmamedov.smscenter.receivers.smpp.BoundState;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionType;
import org.eminmamedov.smscenter.receivers.smpp.SmppRequestProcessor;

public class BindProcessor extends SmppRequestProcessor {

    private static final Logger log = Logger.getLogger(BindProcessor.class);

    @Override
    public void processRequest(SmppConnectionHandler handler, SMPPPacket request) throws IOException {
        Bind bindRequest = convertRequest(request);
        BindResp bindResp = createResponse(bindRequest);
        BoundState boundState = handler.getBoundState();
        if (boundState != BINDING) {
            log.warn("User tries to bound in channel state " + handler.getBoundState());
            if (boundState == BOUND) {
                bindResp.setCommandStatus(ESME_RALYBND);
            } else {
                bindResp.setCommandStatus(ESME_RINVBNDSTS);
            }
        } else {
            SMPPVersion smppVersion = bindRequest.getVersion();
            if (!smppVersion.equals(SMPPVersion.V34)) {
                log.warn("Unsupported SMPP protocol version: " + smppVersion);
                bindResp.setCommandStatus(ESME_RSYSERR);
            } else {
                processBindRequest(handler, bindRequest, bindResp);
            }
        }
        handler.sendResponse(bindResp);
    }

    private void processBindRequest(SmppConnectionHandler handler, Bind bindRequest, BindResp bindResp) {
        String userName = bindRequest.getSystemId();
        String password = bindRequest.getPassword();
        String clientHost = handler.getLink().getClientHost();
        log.info("User trying to connect via SMPP. USER = " + userName + " IP = " + clientHost);
        try {
            User foundUser = getSmsCenterService().getUser(userName, password, clientHost);
            log.debug("User has been found: " + foundUser);
            if (getSmppConnectionPool().handlerExistsAlready(foundUser)) {
                log.info("SMPP channel exists already for user " + foundUser);
                bindResp.setCommandStatus(ESME_RALYBND);
            } else {
                if (foundUser.isEnabled()) {
                    bindResp.setVersion(SMPPVersion.V34);
                    bindResp.setOptionalParameter(Tag.SC_INTERFACE_VERSION, SMPPVersion.V34.getVersionID());
                    handler.setConnectionType(SmppConnectionType.getValueByBindType(bindRequest.getCommandId()));
                    handler.setUser(foundUser);
                    handler.setBoundState(BOUND);
                    log.info("User has been authonticated successfully via SMPP. USER = " + foundUser);
                } else {
                    log.info("User is locked. USER = " + foundUser);
                    bindResp.setCommandStatus(ESME_RINVPASWD);
                }
            }
        } catch (NotFoundException e) {
            log.info("Wrong login, password or ip address. USER = " + userName);
            bindResp.setCommandStatus(ESME_RINVPASWD);
        } catch (RuntimeException e) {
            log.error("Unknown error occured", e);
            bindResp.setCommandStatus(ESME_RSYSERR);
        }
        if (bindResp.getCommandStatus() != 0) {
            log.info("Close SMPP connection handler because wrong bind request has been received. USER = " + userName);
            handler.setBoundState(UNBOUND);
            getSmppConnectionPool().close(handler);
        }
    }

    private BindResp createResponse(Bind request) {
        switch (request.getCommandId()) {
        case SMPPPacket.BIND_TRANSMITTER:
            return new BindTransmitterResp();
        case SMPPPacket.BIND_RECEIVER:
            return new BindReceiverResp();
        case SMPPPacket.BIND_TRANSCEIVER:
            return new BindTransceiverResp();
        default:
            throw new IllegalArgumentException("Unknown command ID");
        }
    }

    private Bind convertRequest(SMPPPacket request) {
        if (request instanceof Bind) {
            return (Bind) request;
        }
        throw new IllegalArgumentException("Unsupported request type");
    }

}
