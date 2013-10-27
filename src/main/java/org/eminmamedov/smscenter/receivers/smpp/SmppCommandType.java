package org.eminmamedov.smscenter.receivers.smpp;

import ie.omk.smpp.message.SMPPPacket;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum SmppCommandType {

    BIND(Arrays.asList(SMPPPacket.BIND_TRANSMITTER, SMPPPacket.BIND_RECEIVER, SMPPPacket.BIND_TRANSCEIVER)),
    UNBIND_RESP(Arrays.asList(SMPPPacket.UNBIND_RESP)),
    UNBIND(Arrays.asList(SMPPPacket.UNBIND)),
    ENQUIRE_LINK(Arrays.asList(SMPPPacket.ENQUIRE_LINK)),
    ENQUIRE_LINK_RESP(Arrays.asList(SMPPPacket.ENQUIRE_LINK_RESP)),
    SUBMIT_SM(Arrays.asList(SMPPPacket.SUBMIT_SM)),
    DELIVER_SM_RESP(Arrays.asList(SMPPPacket.DELIVER_SM_RESP)),
    QUERY_SM(Arrays.asList(SMPPPacket.QUERY_SM));

    private Set<Integer> types;

    private SmppCommandType(Collection<Integer> types) {
        this.types = new HashSet<Integer>(types);
    }

    public Set<Integer> getTypes() {
        return types;
    }

    public static SmppCommandType getValueByTypeCode(int typeCode) {
        SmppCommandType result = null;
        for (SmppCommandType commandType : values()) {
            if (commandType.getTypes().contains(typeCode)) {
                result = commandType;
                break;
            }
        }
        return result;
    }

}
