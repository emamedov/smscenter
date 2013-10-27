package org.eminmamedov.smscenter.receivers.smpp;

import static ie.omk.smpp.message.SMPPPacket.*;

public enum SmppConnectionType {

    TRANSMITTER(BIND_TRANSMITTER), RECEIVER(BIND_RECEIVER), TRANSCEIVER(BIND_TRANSCEIVER);

    private int bindType;

    private SmppConnectionType(int type) {
        this.bindType = type;
    }

    public int getBindType() {
        return bindType;
    }

    public static SmppConnectionType getValueByBindType(int bindType) {
        SmppConnectionType result = null;
        for (SmppConnectionType connectionType : SmppConnectionType.values()) {
            if (connectionType.getBindType() == bindType) {
                result = connectionType;
                break;
            }
        }
        return result;
    }

}
