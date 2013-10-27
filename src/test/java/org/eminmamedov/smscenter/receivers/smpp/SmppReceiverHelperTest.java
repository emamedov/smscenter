package org.eminmamedov.smscenter.receivers.smpp;

import static org.junit.Assert.*;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.version.SMPPVersion;

import org.junit.Before;
import org.junit.Test;

public class SmppReceiverHelperTest {

    private SmppReceiverHelper helper;

    @Before
    public void setUp() throws Exception {
        helper = new SmppReceiverHelper();
    }

    @Test
    public void testNewInstanceOk() throws BadCommandIDException {
        SMPPPacket packet = helper.newInstance(SMPPPacket.ENQUIRE_LINK);
        assertNotNull(packet);
        assertEquals(SMPPVersion.V34, packet.getVersion());
        assertNotNull(packet.getSequenceNum());
        assertTrue(packet instanceof EnquireLink);

        packet = helper.newInstance(SMPPPacket.SUBMIT_SM);
        assertNotNull(packet);
        assertEquals(SMPPVersion.V34, packet.getVersion());
        assertNotNull(packet.getSequenceNum());
        assertTrue(packet instanceof SubmitSM);
    }

    @Test(expected = BadCommandIDException.class)
    public void testNewInstanceBadCommandException() throws BadCommandIDException {
        helper.newInstance(10000);
    }

}
