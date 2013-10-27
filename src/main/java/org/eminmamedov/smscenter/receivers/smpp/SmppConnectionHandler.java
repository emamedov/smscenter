package org.eminmamedov.smscenter.receivers.smpp;

import static org.eminmamedov.smscenter.receivers.smpp.BoundState.*;
import static org.eminmamedov.smscenter.receivers.smpp.SmppCommandType.BIND;
import static org.eminmamedov.smscenter.receivers.smpp.SmppCommandType.SUBMIT_SM;
import static org.eminmamedov.smscenter.receivers.smpp.SmppConnectionType.TRANSMITTER;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.NotBoundException;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPProtocolException;
import ie.omk.smpp.message.SMPPRequest;
import ie.omk.smpp.message.SMPPResponse;
import ie.omk.smpp.message.Unbind;
import ie.omk.smpp.util.PacketFactory;
import ie.omk.smpp.util.SMPPIO;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.eminmamedov.smscenter.datamodel.User;
import org.springframework.beans.factory.annotation.Autowired;

public class SmppConnectionHandler implements Runnable {

    private static final Logger log = Logger.getLogger(SmppConnectionHandler.class);

    private static final int ID_OFFSET = 4;
    private static final int ID_LENGTH = 4;
    private static final int READ_BUFFER_SIZE = 300;

    @Autowired
    private SmppConnectionPool smppConnectionPool;
    @Autowired
    private SmppReceiverHelper smppReceiverHelper;
    @Resource(name = "requestProcessors")
    private Map<SmppCommandType, SmppRequestProcessor> requestProcessors;

    private BoundState boundState;
    private SmsClientLink link;
    private User user = new User();
    private byte[] buf = new byte[READ_BUFFER_SIZE];
    private SmppConnectionType connectionType;
    private AtomicInteger enquireLinkCount = new AtomicInteger(0);

    public SmppConnectionHandler(Socket clientSocket) throws IOException {
        this.boundState = BINDING;
        this.link = new SmsClientLink(clientSocket);
        log.debug("Channel with client has been created successfully");
    }

    @Override
    public void run() {
        try {
            while ((!Thread.interrupted()) && (link.getClientSocket().isConnected()) && (boundState != UNBOUND)) {
                SMPPPacket pak = null;
                try {
                    pak = readNextPacket();
                    if (pak == null) {
                        log.warn("Unknown package type has been received from user " + user.getName());
                        continue;
                    }
                } catch (SMPPProtocolException e) {
                    log.warn("Error in SMPP protocol for user " + user.getName(), e);
                }
            }
        } catch (IOException e) {
            log.warn(e, e);
            smppConnectionPool.close(this);
        } catch (Exception e) {
            log.warn("FATAL UNKNOWN ERROR! PLEASE CHECK LOGS!", e);
            smppConnectionPool.close(this);
        }
    }

    private SMPPPacket readNextPacket() throws IOException, BadCommandIDException {
        this.buf = link.read(this.buf);
        int id = SMPPIO.bytesToInt(this.buf, ID_OFFSET, ID_LENGTH);
        SMPPPacket pak = PacketFactory.newInstance(id);

        if (pak != null) {
            pak.readFrom(this.buf, 0);
            if (log.isDebugEnabled()) {
                StringBuilder b = new StringBuilder("Package has been received: ");
                int packageLength = pak.getLength();
                int commandStatus = pak.getCommandStatus();
                int sequenceNumber = pak.getSequenceNum();
                b.append(pak);
                b.append(" from " + user.getName() + ": ");
                b.append("id:");
                b.append(Integer.toHexString(id));
                b.append(" len:");
                b.append(Integer.toString(packageLength));
                b.append(" st:");
                b.append(Integer.toString(commandStatus));
                b.append(" sq:");
                b.append(Integer.toString(sequenceNumber));
                log.debug(b.toString());
            }
            processInboundPacket(pak);
        }
        return pak;
    }

    private void processInboundPacket(SMPPPacket packet) throws IOException, BadCommandIDException {
        SmppCommandType commandType = SmppCommandType.getValueByTypeCode(packet.getCommandId());
        if (commandType == null) {
            log.warn("Unknown command has been received from user " + user.getName());
            log.warn("Package: " + packet);
            throw new BadCommandIDException("Wrong command id [ " + packet.getCommandId() + " ]");

        }
        requestProcessors.get(commandType).processRequest(this, packet);
    }

    public void sendRequest(SMPPRequest request) throws IOException {
        validateRequest(request);
        log.info("Send request " + request + " to user " + user.getName());
        link.write(request, true);
    }

    private void validateRequest(SMPPRequest request) {
        int id = request.getCommandId();
        if (isNotBound()) {
            throw new NotBoundException("Connection should be in BOUND state to send requests");
        }
        SmppCommandType commandType = SmppCommandType.getValueByTypeCode(id);
        if (commandType == BIND) {
            throw new UnsupportedOperationException("SMSC doesn't allow to send this type of packet. ID=" + id);
        }
        if ((connectionType == TRANSMITTER) && (commandType == SUBMIT_SM)) {
            throw new UnsupportedOperationException(
                    "Connection has been opened in TRANSMITTER mode. It doesn't allow to send this type of packet");
        }
    }

    public void sendResponse(SMPPResponse response) throws IOException {
        log.info("Send response " + response + " to user " + user.getName());
        link.write(response, true);
    }

    public boolean isNotBound() {
        return boundState != BoundState.BOUND;
    }

    public BoundState getBoundState() {
        return boundState;
    }

    public void setBoundState(BoundState boundState) {
        if (boundState == UNBOUNDING) {
            try {
                Unbind req = (Unbind) smppReceiverHelper.newInstance(SMPPPacket.UNBIND);
                sendRequest(req);
            } catch (BadCommandIDException e) {
                log.warn("Unknown command ID", e);
            } catch (UnsupportedOperationException e) {
                log.warn(e, e);
            } catch (IOException e) {
                log.warn(e, e);
            }
        } else if (boundState == UNBOUND) {
            try {
                link.close();
            } catch (IOException e) {
                log.error("Unknown error occured", e);
            }
        }
        this.boundState = boundState;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SmsClientLink getLink() {
        return link;
    }

    public void setConnectionType(SmppConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public void incEnquireLinkCount() {
        this.enquireLinkCount.incrementAndGet();
    }

    public int getEnquireLinkCount() {
        return enquireLinkCount.intValue();
    }

    public void resetEnquireLinkCount() {
        enquireLinkCount.set(0);
    }

}
