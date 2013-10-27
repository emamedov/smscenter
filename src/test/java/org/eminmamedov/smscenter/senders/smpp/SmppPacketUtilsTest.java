package org.eminmamedov.smscenter.senders.smpp;

import static org.junit.Assert.assertEquals;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.BindTransceiverResp;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.message.UnbindResp;
import ie.omk.smpp.message.tlv.Tag;

import java.util.GregorianCalendar;

import org.junit.Test;

public class SmppPacketUtilsTest {

    @Test
    public void testToString_Null() {
        String result = SmppPacketUtils.toString(null);
        assertEquals("null", result);
    }

    @Test
    public void testToString_DeliverSM() {
        DeliverSM dm = new DeliverSM();
        dm.setOptionalParameter(Tag.MESSAGE_STATE, SMPPPacket.SM_STATE_EXPIRED);
        dm.setOptionalParameter(Tag.RECEIPTED_MESSAGE_ID, "000100");
        dm.setMessageText("id:0000100 Text:BlaBla");
        String result = SmppPacketUtils.toString(dm);
        assertEquals("deliver_sm[MessageText: \"id:0000100 Text:BlaBla\"; State: 3; SmscId: 000100]", result);
    }

    @Test
    public void testToString_BindResp() {
        BindResp br = new BindTransceiverResp();
        br.setCommandStatus(3);
        String result = SmppPacketUtils.toString(br);
        assertEquals("bind_transceiver_resp[CommandStatus: 3]", result);
    }

    @Test
    public void testToString_SubmitSMResp() {
        SubmitSMResp smr = new SubmitSMResp();
        smr.setSequenceNum(100);
        smr.setMessageId("000100");
        smr.setCommandStatus(3);
        String result = SmppPacketUtils.toString(smr);
        assertEquals("submit_sm_resp[SeqNm: 100; MessageId: 000100; CommandStatus: 3]", result);
    }

    @Test
    public void testToString_SMPPResponse() {
        UnbindResp ur = new UnbindResp();
        ur.setMessageId("000100");
        ur.setMessageStatus(3);
        ur.setFinalDate(new GregorianCalendar(2012, 4, 5).getTime());
        String result = SmppPacketUtils.toString(ur);
        assertEquals("unbind_resp[MessageId: 000100; MessageStatus: 3; FinalDate: 5/5/12 0:0:0]", result);
    }

}
