package org.eminmamedov.smscenter.receivers.smpp;

import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.util.DefaultSequenceScheme;
import ie.omk.smpp.util.PacketFactory;
import ie.omk.smpp.util.SequenceNumberScheme;
import ie.omk.smpp.version.SMPPVersion;

import org.springframework.stereotype.Component;

@Component
public class SmppReceiverHelper {

    private SequenceNumberScheme seqNumScheme;

    public SmppReceiverHelper() {
        this.seqNumScheme = new DefaultSequenceScheme();
    }

    public SMPPPacket newInstance(int commandId) throws BadCommandIDException {
        SMPPPacket packet = PacketFactory.newInstance(commandId);
        packet.setVersion(SMPPVersion.V34);
        packet.setSequenceNum(this.seqNumScheme.nextNumber());
        return packet;
    }

}
