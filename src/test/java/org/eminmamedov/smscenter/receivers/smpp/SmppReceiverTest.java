package org.eminmamedov.smscenter.receivers.smpp;

import static junit.framework.Assert.*;
import ie.omk.smpp.Address;
import ie.omk.smpp.Connection;
import ie.omk.smpp.event.SMPPEventAdapter;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.util.Latin1Encoding;
import ie.omk.smpp.version.SMPPVersion;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eminmamedov.smscenter.common.SpringTestSupport;
import org.eminmamedov.smscenter.dao.SenderMapper;
import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.dao.UserMapper;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class SmppReceiverTest extends SpringTestSupport {

    @Autowired
    private SmppReceiver smppReceiver;
    @Value("${smscenter.receiver.smpp.port}")
    private int serverPort;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SenderMapper senderMapper;
    @Autowired
    private SmsMapper smsMapper;
    private User user;
    private Sender sender;
    private Long messageId;

    @Before
    public void setUp() {
        user = new User();
        user.setName("testUser");
        user.setPassword("testPass");
        user.setEnabled(true);
        userMapper.insert(user);

        sender = new Sender();
        sender.setSign("testSender");
        senderMapper.insert(sender);

        senderMapper.createLink(user, sender);
    }

    @After
    public void tearDown() {
        if (messageId != null) {
            SMSMessage message = new SMSMessage();
            message.setId(messageId);
            smsMapper.delete(message);
        }
        senderMapper.removeLinks(sender);
        userMapper.delete(user);
        senderMapper.delete(sender);
    }

    @Test
    public void testSmppReceiver() throws Exception {
        final AtomicInteger smrCount = new AtomicInteger(0);

        // Connect to the SMPP port
        Connection smppCon = new Connection("localhost", serverPort, true);
        smppCon.addObserver(new SMPPEventAdapter() {
            @Override
            public void submitSMResponse(Connection source, SubmitSMResp smr) {
                messageId = Long.valueOf(smr.getMessageId());
                assertNotNull(messageId);
                assertTrue(messageId > 0);
                System.out.println("Message has been sent [" + messageId + "]");
                smrCount.incrementAndGet();
            }
        });
        smppCon.autoAckLink(true);
        smppCon.autoAckMessages(true);
        smppCon.setVersion(SMPPVersion.V34);
        smppCon.bind(Connection.TRANSCEIVER, user.getName(), user.getPassword(), null);

        int count = 0;
        while (!(smppCon.isBound()) && (count < 10)) {
            Thread.sleep(1000);
            count++;
        }

        SubmitSM messageRequest = new SubmitSM();
        messageRequest.setSequenceNum(10);
        messageRequest.setDestination(new Address(0, 0, "79853869839"));
        messageRequest.setSource(new Address(0, 0, "testSender"));
        messageRequest.setMessageText("TEST MESSAGE");
        messageRequest.setRegistered(1);
        messageRequest.setMessageEncoding(new Latin1Encoding());
        smppCon.sendRequest(messageRequest);

        count = 0;
        while ((smrCount.get() == 0) && (count < 10)) {
            Thread.sleep(1000);
            count++;
        }

        smppCon.unbind();

        assertEquals(1, smrCount.get());

        List<SMSMessage> messages = smsMapper.getMessages(Arrays.asList(messageId));
        assertNotNull(messages);
        assertEquals(1, messages.size());
        SMSMessage message = messages.get(0);
        assertEquals(10, message.getClientId());
        assertEquals("79853869839", message.getPhone());
        assertEquals("testSender", message.getSenderSign());
        assertEquals("TEST MESSAGE", message.getText());
        assertEquals("testSender", message.getSender().getSign());
        assertEquals("testUser", message.getUser().getName());
    }

}
