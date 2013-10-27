package org.eminmamedov.smscenter.receivers.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eminmamedov.smscenter.common.DbValueUtils;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.junit.Test;

public class MessageConverterTest {

    private MessageConverter messageConverter = new MessageConverter();

    @Test
    public void testCreateSendMessageResponseOk() {
        SMSMessage smsMessage = new SMSMessage();
        smsMessage.setId(1L);
        smsMessage.setClientId(2);
        smsMessage.setText("TEXT");
        smsMessage.setSmsGroupCount(3);
        smsMessage.setSmsGroupIndex(4);

        Message message = messageConverter.createSendMessageResponse(smsMessage);
        assertNotNull(message);
        assertEquals(smsMessage.getId(), message.getServerId());
        assertEquals(new Integer(smsMessage.getClientId()), message.getClientId());
        assertEquals(new Integer(smsMessage.getSmsGroupCount()), message.getGroupCount());
        assertEquals(new Integer(smsMessage.getSmsGroupIndex()), message.getGroupIndex());
        assertEquals(smsMessage.getText(), message.getText());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateSendMessageResponseNull() {
        messageConverter.createSendMessageResponse(null);
    }

    @Test
    public void testCreateCheckMessageResponseOk() {
        SMSMessage smsMessage = new SMSMessage();
        smsMessage.setId(1L);
        smsMessage.setClientId(2);
        smsMessage.setStatus(MessageStatus.MESSAGE_WAS_DELIVERED);
        smsMessage.setSmsGroupCount(3);
        smsMessage.setSmsGroupIndex(4);

        Message message = messageConverter.createCheckMessageResponse(smsMessage);
        assertNotNull(message);
        assertEquals(smsMessage.getId(), message.getServerId());
        assertEquals(new Integer(smsMessage.getClientId()), message.getClientId());
        assertEquals(DbValueUtils.getDbValue(smsMessage.getStatus()), message.getStatus());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateCheckMessageResponseNull() {
        messageConverter.createCheckMessageResponse(null);
    }

}
