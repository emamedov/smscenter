package org.eminmamedov.smscenter.senders.smpp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eminmamedov.smscenter.common.SpringTestSupport;
import org.eminmamedov.smscenter.common.WorkerInitializer;
import org.eminmamedov.smscenter.dao.SenderMapper;
import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.dao.UserMapper;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class SmppSmsSenderTest extends SpringTestSupport {

    @Value("${smscenter.receiver.smpp.port}")
    private int serverPort;
    @Autowired
    private WorkerInitializer workerInitializer;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private SenderMapper senderMapper;
    @Autowired
    private SmsCenterService service;
    private SmppSmsSender smppSmsSender;
    private Channel channel;
    private User user;
    private Sender sender;
    private SMSMessage message;

    @Before
    public void setUp() throws Exception {
        createUser();
        createSender();
        userMapper.insert(user);
        senderMapper.insert(sender);
        senderMapper.createLink(user, sender);
        createSmsMessage(service.reserveServerId(), "79050533953", "TEST MESSAGE", "TEST_SIGN");
        smsMapper.addMessages(Arrays.asList(message));

        this.channel = createChannel();
        this.smppSmsSender = new SmppSmsSender(channel);
        workerInitializer.autowire(smppSmsSender);
    }

    @Test
    public void testConnect_DisabledChannel() {
        channel.setEnabled(false);
        smppSmsSender.connect();
        assertSame(channel, smppSmsSender.getChannel());
        assertFalse(smppSmsSender.isConnected());
        assertFalse(smppSmsSender.isBound());
    }

    @Test
    public void testConnect_EnabledChannel_WrongPassword() throws Exception {
        channel.setEnabled(true);
        channel.setPassword("wrongPas");

        smppSmsSender.connect();

        assertSame(channel, smppSmsSender.getChannel());
        assertTrue(smppSmsSender.isConnected());
        // wait 3 secs while sender will be not bound
        int count = 0;
        while (!smppSmsSender.isBound() && count < 3) {
            count++;
            Thread.sleep(1000);
        }
        assertFalse(smppSmsSender.isBound());
    }

    @Test
    public void testConnect_EnabledChannel_ShouldBound() throws Exception {
        channel.setEnabled(true);

        smppSmsSender.connect();

        assertSame(channel, smppSmsSender.getChannel());
        assertTrue(smppSmsSender.isConnected());
        // wait 3 secs while sender will be not bound
        int count = 0;
        while (!smppSmsSender.isBound() && count < 3) {
            count++;
            Thread.sleep(1000);
        }
        assertTrue(smppSmsSender.isBound());
    }

    @Test
    public void testSendPingPackage() throws Exception {
        smppSmsSender.sendPingPackage();

        channel.setEnabled(true);
        smppSmsSender.connect();
        assertTrue(smppSmsSender.isConnected());
        // wait 3 secs while sender will be not bound
        int count = 0;
        while (!smppSmsSender.isBound() && count < 3) {
            count++;
            Thread.sleep(1000);
        }
        assertTrue(smppSmsSender.isBound());

        smppSmsSender.sendPingPackage();
    }

    @Test
    public void testCheckMessages_NotBound() {
        smppSmsSender.checkMessages();
        assertFalse(smppSmsSender.isConnected());
        assertFalse(smppSmsSender.isBound());
    }

    @Test
    public void testCheckMessages_NoMessagesToCheck() throws Exception {
        SmsMapper smsMapper = mock(SmsMapper.class);
        Whitebox.setInternalState(smppSmsSender, "smsMapper", smsMapper);
        when(
                smsMapper.getMessagesByState(same(channel), eq(MessageStatus.MESSAGE_IN_QUEUE),
                        eq(channel.getCheckSpeed()), any(Date.class))).thenReturn(null);

        channel.setEnabled(true);
        smppSmsSender.connect();
        assertTrue(smppSmsSender.isConnected());
        // wait 3 secs while sender will be not bound
        int count = 0;
        while (!smppSmsSender.isBound() && count < 3) {
            count++;
            Thread.sleep(1000);
        }
        assertTrue(smppSmsSender.isBound());

        smppSmsSender.checkMessages();
        verify(smsMapper, times(1)).getMessagesByState(same(channel), eq(MessageStatus.MESSAGE_IN_QUEUE),
                eq(channel.getCheckSpeed()), any(Date.class));
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testCheckMessages_MessagesToCheckExist() throws Exception {
        SmsMapper smsMapper = mock(SmsMapper.class);
        Whitebox.setInternalState(smppSmsSender, "smsMapper", smsMapper);

        SMSMessage smsMessage1 = new SMSMessage();
        smsMessage1.setId(1L);
        smsMessage1.setSenderSign("TEST_SIGN");
        smsMessage1.setSmscId(message.getId().toString());

        SMSMessage smsMessage2 = new SMSMessage();
        smsMessage2.setId(1L);
        smsMessage2.setSender(sender);
        smsMessage2.setSmscId(message.getId().toString());

        when(
                smsMapper.getMessagesByState(same(channel), eq(MessageStatus.MESSAGE_IN_QUEUE),
                        eq(channel.getCheckSpeed()), any(Date.class))).thenReturn(
                Arrays.asList(new SMSMessage(), smsMessage1, smsMessage2));

        channel.setEnabled(true);
        smppSmsSender.connect();
        assertTrue(smppSmsSender.isConnected());
        // wait 3 secs while sender will be not bound
        int count = 0;
        while (!smppSmsSender.isBound() && count < 3) {
            count++;
            Thread.sleep(1000);
        }
        assertTrue(smppSmsSender.isBound());

        smppSmsSender.checkMessages();
        verify(smsMapper, times(1)).getMessagesByState(same(channel), eq(MessageStatus.MESSAGE_IN_QUEUE),
                eq(channel.getCheckSpeed()), any(Date.class));
        verify(smsMapper, times(2)).update(any(SMSMessage.class));
    }

    @Test
    public void testSendMessages_NotBound() {
        smppSmsSender.sendMessages();
        assertFalse(smppSmsSender.isConnected());
        assertFalse(smppSmsSender.isBound());
    }

    @Test
    public void testSendMessages_NoMessagesToSend() throws Exception {
        SmsMapper smsMapper = mock(SmsMapper.class);
        Whitebox.setInternalState(smppSmsSender, "smsMapper", smsMapper);
        when(
                smsMapper.getMessagesByState(same(channel), eq(MessageStatus.NEW_MESSAGE_STATE),
                        eq(channel.getCheckSpeed()), any(Date.class))).thenReturn(null);
        when(
                smsMapper.getMessagesByState(same(channel), eq(MessageStatus.MESSAGE_RESERVED_BY_CHANNEL),
                        eq(channel.getCheckSpeed()), any(Date.class))).thenReturn(null);

        channel.setEnabled(true);
        smppSmsSender.connect();
        assertTrue(smppSmsSender.isConnected());
        // wait 3 secs while sender will be not bound
        int count = 0;
        while (!smppSmsSender.isBound() && count < 3) {
            count++;
            Thread.sleep(1000);
        }
        assertTrue(smppSmsSender.isBound());

        smppSmsSender.sendMessages();
        verify(smsMapper, times(1)).getMessagesByState(same(channel), eq(MessageStatus.NEW_MESSAGE_STATE),
                eq(channel.getCheckSpeed()), any(Date.class));
        verify(smsMapper, times(1)).getMessagesByState(same(channel), eq(MessageStatus.MESSAGE_RESERVED_BY_CHANNEL),
                eq(channel.getCheckSpeed()), any(Date.class));
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testSendMessages_MessagesToSendExist() throws Exception {
        SmsMapper smsMapper = mock(SmsMapper.class);
        Whitebox.setInternalState(smppSmsSender, "smsMapper", smsMapper);

        SMSMessage smsMessage4 = new SMSMessage();
        smsMessage4.setId(4L);
        smsMessage4.setSenderSign("TEST_SIGN");

        SMSMessage smsMessage1 = new SMSMessage();
        smsMessage1.setId(1L);
        smsMessage1.setPhone("79050533954");
        smsMessage1.setSenderSign("TEST_SIGN");

        SMSMessage smsMessage2 = new SMSMessage();
        smsMessage2.setId(2L);
        smsMessage2.setPhone("79050533956");
        smsMessage2.setText("Test text");
        smsMessage2.setSenderSign("TEST_SIGN");

        SMSMessage smsMessage3 = new SMSMessage();
        smsMessage3.setId(3L);
        smsMessage3.setPhone("79050533955");
        smsMessage3.setText("Test text");
        smsMessage3.setSender(sender);
        smsMessage3.setSmsGroupCount(1);
        smsMessage3.setSmsGroupId(1);
        smsMessage3.setSmsGroupIndex(1);

        List<SMSMessage> messagesToSend = Arrays.asList(smsMessage1, smsMessage2, smsMessage3, smsMessage4);
        when(
                smsMapper.getMessagesByState(same(channel), eq(MessageStatus.NEW_MESSAGE_STATE),
                        eq(channel.getCheckSpeed()), any(Date.class))).thenReturn(messagesToSend);
        when(
                smsMapper.getMessagesByState(same(channel), eq(MessageStatus.MESSAGE_RESERVED_BY_CHANNEL),
                        eq(channel.getCheckSpeed()), any(Date.class))).thenReturn(messagesToSend);

        channel.setEnabled(true);
        smppSmsSender.connect();
        assertTrue(smppSmsSender.isConnected());
        // wait 3 secs while sender will be not bound
        int count = 0;
        while (!smppSmsSender.isBound() && count < 3) {
            count++;
            Thread.sleep(1000);
        }
        assertTrue(smppSmsSender.isBound());

        smppSmsSender.sendMessages();
        verify(smsMapper, times(1)).getMessagesByState(same(channel), eq(MessageStatus.NEW_MESSAGE_STATE),
                eq(channel.getCheckSpeed()), any(Date.class));
        verify(smsMapper, times(1)).getMessagesByState(same(channel), eq(MessageStatus.MESSAGE_RESERVED_BY_CHANNEL),
                eq(channel.getCheckSpeed()), any(Date.class));
        verify(smsMapper, times(8)).update(any(SMSMessage.class));
        Thread.sleep(2000);

        List<SMSMessage> messagesByClientIds = this.smsMapper.getMessagesByClientIds(user, Arrays.asList(1L, 2L, 3L));
        assertNotNull(messagesByClientIds);
        assertEquals(2, messagesByClientIds.size());
        assertEqualsMessages(smsMessage2, findByClientId(messagesByClientIds, smsMessage2.getId()));
        assertEqualsMessages(smsMessage3, findByClientId(messagesByClientIds, smsMessage3.getId()));
    }

    @After
    public void tearDown() throws Exception {
        if (smppSmsSender.isConnected()) {
            smppSmsSender.closeConnection();
        }
        List<SMSMessage> messagesByClientIds = this.smsMapper.getMessagesByClientIds(user, Arrays.asList(1L, 2L, 3L));
        for (SMSMessage mes : messagesByClientIds) {
            this.smsMapper.delete(mes);
        }
        smsMapper.delete(message);
        senderMapper.removeLinks(sender);
        senderMapper.delete(sender);
        userMapper.delete(user);
    }

    private Channel createChannel() {
        Channel result = new Channel();
        result.setBindType("cpa");
        result.setCheckSpeed(10);
        result.setSendSpeed(10);
        result.setHost("localhost");
        result.setId(1L);
        result.setLogin(user.getName());
        result.setPassword(user.getPassword());
        result.setName("TEST_CHANNEL");
        result.setPort(serverPort);
        return result;
    }

    private void createUser() {
        this.user = new User();
        user.setEnabled(true);
        user.setName("testUser");
        user.setPassword("testPass");
    }

    private void createSender() {
        this.sender = new Sender();
        sender.setSign("TEST_SIGN");
    }

    private SMSMessage createSmsMessage(Long id, String phone, String text, String senderSign) {
        this.message = new SMSMessage();
        message.setPhone(phone);
        message.setText(text);
        message.setId(id);
        message.setClientId(id.intValue() + 10);
        message.setSendDate(new Date());
        message.setSenderSign(senderSign);
        message.setSmscId(null);
        message.setSmsGroupCount(0);
        message.setSmsGroupId(1);
        message.setSmsGroupIndex(2);
        message.setStatus(MessageStatus.MESSAGE_WAS_DELIVERED);
        message.setUser(user);
        message.setSender(this.sender);
        return message;
    }

    private void assertEqualsMessages(SMSMessage expected, SMSMessage actual) {
        assertEquals(expected.getPhone(), actual.getPhone());
        assertEquals(expected.getText(), actual.getText());
        assertEquals(channel.getLogin(), actual.getUser().getName());
        assertEquals("TEST_SIGN", actual.getSender().getSign());
    }

    private SMSMessage findByClientId(List<SMSMessage> messagesByClientIds, Long clientId) {
        for (SMSMessage mes : messagesByClientIds) {
            if (clientId.intValue() == mes.getClientId()) {
                return mes;
            }
        }
        return null;
    }

}
