package org.eminmamedov.smscenter.senders.smpp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import ie.omk.smpp.message.SMPPPacket;

import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.junit.Before;
import org.junit.Test;

public class MessageStatusConverterTest {

    private MessageStatusConverter converter;

    @Before
    public void setUp() {
        this.converter = new MessageStatusConverter();
    }

    @Test
    public void testConvertToMessageStatusString_Null() {
        assertNull(converter.convertToMessageStatus(null));
    }

    @Test
    public void testConvertToMessageStatusString_DELIVRD() {
        MessageStatus status = converter.convertToMessageStatus("DELIVRD");
        assertEquals(MessageStatus.MESSAGE_WAS_DELIVERED, status);
    }

    @Test
    public void testConvertToMessageStatusString_UNDELIV() {
        MessageStatus status = converter.convertToMessageStatus("UNDELIV");
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, status);
    }

    @Test
    public void testConvertToMessageStatusString_EXPIRED() {
        MessageStatus status = converter.convertToMessageStatus("EXPIRED");
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, status);
    }

    @Test
    public void testConvertToMessageStatusString_Other() {
        assertNull(converter.convertToMessageStatus("OTHER"));
    }

    @Test
    public void testConvertToMessageStatusInteger_DELIVERED() {
        MessageStatus status = converter.convertToMessageStatus(SMPPPacket.SM_STATE_DELIVERED);
        assertEquals(MessageStatus.MESSAGE_WAS_DELIVERED, status);
    }

    @Test
    public void testConvertToMessageStatusInteger_UNDELIVERABLE() {
        MessageStatus status = converter.convertToMessageStatus(SMPPPacket.SM_STATE_UNDELIVERABLE);
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, status);
    }

    @Test
    public void testConvertToMessageStatusInteger_EXPIRED() {
        MessageStatus status = converter.convertToMessageStatus(SMPPPacket.SM_STATE_EXPIRED);
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, status);
    }

    @Test
    public void testConvertToMessageStatusInteger_OTHER() {
        MessageStatus status = converter.convertToMessageStatus(SMPPPacket.SM_STATE_EN_ROUTE);
        assertNull(status);
    }

}