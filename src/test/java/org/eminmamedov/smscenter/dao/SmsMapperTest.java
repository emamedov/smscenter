package org.eminmamedov.smscenter.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eminmamedov.smscenter.common.SpringTestSupport;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

@TransactionConfiguration(defaultRollback = true)
public class SmsMapperTest extends SpringTestSupport {

    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SenderMapper senderMapper;
    @Autowired
    private ChannelMapper channelMapper;
    private List<SMSMessage> messagesList;
    private Integer lastId;
    private User testUser;
    private Sender testSender;
    private Channel testChannel;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setName("SMS_TEST");
        testUser.setPassword("test");
        testUser.setEnabled(true);
        userMapper.insert(testUser);

        testSender = new Sender();
        testSender.setSign("TEST_SENDER");
        senderMapper.insert(testSender); 
        senderMapper.createLink(testUser, testSender);

        testChannel = new Channel();
        testChannel.setName("Beeline");
        testChannel.setLogin("testBee");
        testChannel.setPassword("testBeePass");
        testChannel.setPort(80);
        testChannel.setEnabled(true);
        testChannel.setHost("123");
        testChannel.setSendSpeed(100);
        testChannel.setCheckSpeed(110);
        testChannel.setBindType("1");
        channelMapper.insert(testChannel);
        channelMapper.createLink(testChannel, testSender);

        messagesList = new ArrayList<SMSMessage>();
        lastId = smsMapper.getLastSmsId();
        lastId = lastId == null ? 0 : lastId;
        for (int i = 0; i < 10; i++) {
            messagesList.add(createSmsMessage(++lastId, "79853869839", "MESSAGE" + lastId, "SENDER" + lastId));
        }
        smsMapper.addMessages(messagesList);
    }

    @Test
    public void testSmsMapper() {
        List<Long> messageIds = new ArrayList<Long>();
        messageIds.add(messagesList.get(2).getId());
        messageIds.add(messagesList.get(5).getId());
        messageIds.add(messagesList.get(6).getId());
        messageIds.add(messagesList.get(8).getId());
        // not existing ID
        messageIds.add(new Long(lastId + 1000));

        List<SMSMessage> foundMessages = smsMapper.getMessages(messageIds);
        int count = 0;
        for (SMSMessage expectedMessage : messagesList) {
            for (SMSMessage actualMessage : foundMessages) {
                if (expectedMessage.getId().equals(actualMessage.getId())) {
                    count++;
                    assertEqualsSmsMessages(expectedMessage, actualMessage);
                }
            }
        }
        assertEquals(messageIds.size() - 1, count);

        List<SMSMessage> foundByState = smsMapper.getMessagesByState(testChannel, MessageStatus.MESSAGE_WAS_DELIVERED, 3, new Date());
        assertEquals(3, foundByState.size());

        SMSMessage message = foundByState.get(1);
        message.setStatus(MessageStatus.MESSAGE_IN_QUEUE);
        smsMapper.update(message);
        SMSMessage updatedMessage = smsMapper.getMessage(message.getId());
        assertEqualsSmsMessages(message, updatedMessage);
        assertNull(updatedMessage.getChannel());

        message = smsMapper.getMessage(message.getId());
        message.setStatus(MessageStatus.MESSAGE_WASNT_DELIVERED);
        message.setChannel(testChannel);
        smsMapper.update(message);
        updatedMessage = smsMapper.getMessage(message.getId());
        assertEqualsSmsMessages(message, updatedMessage);
        assertNotNull(updatedMessage.getChannel());
        assertEquals(testChannel.getId(), updatedMessage.getChannel().getId());

        SMSMessage foundMessage = smsMapper.getMessage(messagesList.get(2).getId());
        assertEqualsSmsMessages(messagesList.get(2), foundMessage);

        smsMapper.setInformed(messagesList.get(2).getId(), false);
        smsMapper.setInformed(messagesList.get(7).getId(), false);
        List<SMSMessage> updatedMessages = smsMapper.getUpdatedMessages(testUser, 3);
        assertFalse(updatedMessages.isEmpty());
        assertEquals(2, updatedMessages.size());
    }

    private void assertEqualsSmsMessages(SMSMessage expected, SMSMessage actual) {
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getClientId(), actual.getClientId());
        assertEquals(expected.getPhone(), actual.getPhone());
        assertEquals(expected.getSendDate().getTime() / 1000, actual.getSendDate().getTime() / 1000);
        assertEquals(expected.getSender().getId(), actual.getSender().getId());
        assertEquals(expected.getSender().getSign(), actual.getSender().getSign());
        assertEquals(expected.getSenderSign(), actual.getSenderSign());
        assertEquals("0", actual.getSmscId());
        assertEquals(expected.getSmsGroupCount(), actual.getSmsGroupCount());
        assertEquals(expected.getSmsGroupId(), actual.getSmsGroupId());
        assertEquals(expected.getSmsGroupIndex(), actual.getSmsGroupIndex());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getText(), actual.getText());
        assertNotNull(actual.getUser());
        assertEquals(expected.getUser().getId(), actual.getUser().getId());
    }

    private SMSMessage createSmsMessage(Integer id, String phone, String text, String sender) {
        SMSMessage message = new SMSMessage();
        message.setPhone(phone);
        message.setText(text);
        message.setId(new Long(id));
        message.setClientId(id);
        message.setSendDate(new Date());
        message.setSenderSign(sender);
        message.setSmscId(null);
        message.setSmsGroupCount(0);
        message.setSmsGroupId(1);
        message.setSmsGroupIndex(2);
        message.setStatus(MessageStatus.MESSAGE_WAS_DELIVERED);
        message.setUser(testUser);
        message.setSender(testSender);
        return message;
    }

    @After
    public void tearDown() {
        if (messagesList != null) {
            for (SMSMessage message : messagesList) {
                smsMapper.delete(message);
            }
        }
        senderMapper.removeLinks(testSender);
        channelMapper.removeLinks(testChannel);
        userMapper.delete(testUser);
        senderMapper.delete(testSender);
        channelMapper.delete(testChannel);
    }

}
