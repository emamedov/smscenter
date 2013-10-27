package org.eminmamedov.smscenter.senders.smpp;

import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPResponse;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.message.tlv.Tag;
import ie.omk.smpp.util.SMPPDate;

public final class SmppPacketUtils {

    private SmppPacketUtils() {

    }

    public static String toString(SMPPPacket pack) {
        StringBuilder sb = new StringBuilder();
        sb.append(pack);
        if (pack instanceof DeliverSM) {
            DeliverSM dm = (DeliverSM) pack;
            sb.append("[");
            sb.append("MessageText: \"");
            sb.append(dm.getMessageText());
            sb.append("\"; ");
            sb.append("State: ");
            sb.append(dm.getOptionalParameter(Tag.MESSAGE_STATE));
            sb.append("; ");
            sb.append("SmscId: ");
            sb.append(dm.getOptionalParameter(Tag.RECEIPTED_MESSAGE_ID));
            sb.append("]");
        } else if (pack instanceof BindResp) {
            BindResp br = (BindResp) pack;
            sb.append("[");
            sb.append("CommandStatus: ");
            sb.append(br.getCommandStatus());
            sb.append("]");
        } else if (pack instanceof SubmitSMResp) {
            SubmitSMResp smr = (SubmitSMResp) pack;
            sb.append("[");
            sb.append("SeqNm: ");
            sb.append(smr.getSequenceNum());
            sb.append("; ");
            sb.append("MessageId: ");
            sb.append(smr.getMessageId());
            sb.append("; ");
            sb.append("CommandStatus: ");
            sb.append(smr.getCommandStatus());
            sb.append("]");
        } else if (pack instanceof SMPPResponse) {
            SMPPResponse sr = (SMPPResponse) pack;
            sb.append("[");
            sb.append("MessageId: ");
            sb.append(sr.getMessageId());
            sb.append("; ");
            sb.append("MessageStatus: ");
            sb.append(sr.getMessageStatus());
            sb.append("; ");
            sb.append("FinalDate: ");
            convertDateToString(sb, sr.getFinalDate());
            sb.append("]");
        }
        return sb.toString();
    }

    private static void convertDateToString(StringBuilder sb, SMPPDate finalDate) {
        if (finalDate == null) {
            sb.append("null");
        } else {
            sb.append(finalDate.getDay());
            sb.append("/");
            sb.append(finalDate.getMonth());
            sb.append("/");
            sb.append(finalDate.getYear());
            sb.append(" ");
            sb.append(finalDate.getHour());
            sb.append(":");
            sb.append(finalDate.getMinute());
            sb.append(":");
            sb.append(finalDate.getSecond());
        }
    }

}
