package org.eminmamedov.smscenter.receivers.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eminmamedov.smscenter.common.SmsCenterUtils;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpReceiverHelperTest {

    @Mock
    private SmsCenterService smsCenterService;
    @Mock
    private SmsCenterUtils smsCenterUtils;
    private HttpReceiverHelper httpReceiverHelper;

    @Before
    public void setUp() {
        httpReceiverHelper = new HttpReceiverHelper();
        httpReceiverHelper.setMessageConverter(new MessageConverter());
        httpReceiverHelper.setSmsCenterService(smsCenterService);
        when(smsCenterUtils.checkNumber(any(String.class))).thenCallRealMethod();
        httpReceiverHelper.setSmsCenterUtils(smsCenterUtils);
    }

    @Test(expected = NullPointerException.class)
    public void testCheckMessagesNull() {
        httpReceiverHelper.checkMessages(null, null);
    }

    @Test
    public void testCheckMessagesEmptyList() {
        List<Message> checkMessages = httpReceiverHelper.checkMessages(null, Collections.<Message> emptyList());
        assertNotNull(checkMessages);
        assertTrue(checkMessages.isEmpty());
    }

    @Test
    public void testCheckMessagesNotFound() {
        Message message1 = createCheckMessage(1);
        Message message2 = createCheckMessage(3);
        Message message3 = createCheckMessage(5);
        when(smsCenterService.getMessages(anyListOf(Long.class))).thenReturn(Collections.<SMSMessage> emptyList());
        List<Message> checkMessages = httpReceiverHelper.checkMessages(null,
                Arrays.asList(message1, message2, message3));
        assertNotNull(checkMessages);
        assertTrue(checkMessages.isEmpty());
    }

    @Test
    public void testCheckMessagesFound() {
        Message message1 = createCheckMessage(1);
        Message message2 = createCheckMessage(3);
        Message message3 = createCheckMessage(5);
        SMSMessage smsMessage = createSmsMessage(2L);
        when(smsCenterService.getMessages(anyListOf(Long.class))).thenReturn(Arrays.asList(smsMessage));
        List<Message> checkMessages = httpReceiverHelper.checkMessages(null,
                Arrays.asList(message1, message2, message3));
        assertNotNull(checkMessages);
        assertFalse(checkMessages.isEmpty());
        assertEquals(1, checkMessages.size());
    }

    @Test(expected = NullPointerException.class)
    public void testSendMessagesNull() {
        httpReceiverHelper.sendMessages(null, null);
    }

    @Test
    public void testSendMessagesEmptyList() {
        List<Message> sendMessages = httpReceiverHelper.sendMessages(null, Collections.<Message> emptyList());
        assertNotNull(sendMessages);
        assertTrue(sendMessages.isEmpty());
    }

    @Test
    public void testSendMessagesNotEmptyListWrongSms() {
        when(smsCenterService.getSender(any(User.class), any(String.class))).thenReturn(null);

        Message wrongTextMessage = createSendMessage(1, "", "79853869839", "test");
        Message wrongPhoneMessage = createSendMessage(2, "TEXT_OK", "ABC789123", "test");
        Message wrongSenderMessage = createSendMessage(3, "TEXT_OK", "79853869839", "not_exist");
        List<Message> sendMessages = httpReceiverHelper.sendMessages(null,
                Arrays.asList(wrongTextMessage, wrongPhoneMessage, wrongSenderMessage));
        assertNotNull(sendMessages);
        assertFalse(sendMessages.isEmpty());
        assertEquals(3, sendMessages.size());
        Message message = getMessageById(sendMessages, 1);
        assertEquals(new Integer(HttpReceiverErrorCodes.ERR_INV_TEXT_LENGTH), message.getErrorCode());
        message = getMessageById(sendMessages, 2);
        assertEquals(new Integer(HttpReceiverErrorCodes.ERR_INV_DST_ADR), message.getErrorCode());
        message = getMessageById(sendMessages, 3);
        assertEquals(new Integer(HttpReceiverErrorCodes.ERR_INV_SRC_ADR), message.getErrorCode());
        verify(smsCenterService, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testSendMessagesNotEmptyListOk() {
        when(smsCenterService.reserveServerId()).thenReturn(new Long(10));
        when(smsCenterService.getSender(any(User.class), any(String.class))).thenReturn(new Sender());
        when(smsCenterUtils.splitMessage(any(String.class))).thenReturn(Arrays.asList("TEXT_OK"));

        Message messageOk = createSendMessage(1, "TEXT_OK", "79853869839", "test");
        List<Message> sendMessages = httpReceiverHelper.sendMessages(new User(), Arrays.asList(messageOk));
        assertNotNull(sendMessages);
        assertFalse(sendMessages.isEmpty());
        assertEquals(1, sendMessages.size());
        Message message = getMessageById(sendMessages, 1);
        assertNull(message.getErrorCode());
        assertEquals(new Long(10), message.getServerId());
        verify(smsCenterService, times(1)).addMessages(anyListOf(SMSMessage.class));
    }

    private Message getMessageById(List<Message> sendMessages, int id) {
        for (Message message : sendMessages) {
            if (message.getClientId() == id) {
                return message;
            }
        }
        return null;
    }

    private Message createSendMessage(int id, String text, String phone, String sender) {
        Message message = new Message();
        message.setClientId(id);
        message.setText(text);
        message.setReceiver(phone);
        message.setSender(sender);
        return message;
    }

    private SMSMessage createSmsMessage(Long id) {
        SMSMessage smsMessage = new SMSMessage();
        smsMessage.setId(id);
        smsMessage.setClientId(new Long(id + 1).intValue());
        smsMessage.setStatus(MessageStatus.MESSAGE_IN_QUEUE);
        smsMessage.setSmsGroupCount(new Long(id + 2).intValue());
        smsMessage.setSmsGroupIndex(new Long(id + 3).intValue());
        smsMessage.setText("TEST");
        return smsMessage;
    }

    private Message createCheckMessage(int id) {
        Message message = new Message();
        message.setClientId(id);
        message.setServerId(new Long(id + 1));
        return message;
    }

}
