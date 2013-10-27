package org.eminmamedov.smscenter.receivers.smpp;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import ie.omk.smpp.Connection;
import ie.omk.smpp.event.SMPPEventAdapter;
import ie.omk.smpp.message.BindTransmitter;
import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.EnquireLinkResp;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.Unbind;
import ie.omk.smpp.version.SMPPVersion;

import java.util.concurrent.atomic.AtomicInteger;

import org.eminmamedov.smscenter.common.SpringTestSupport;
import org.eminmamedov.smscenter.dao.SenderMapper;
import org.eminmamedov.smscenter.dao.UserMapper;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

public class SmppConnectionHandlerTest extends SpringTestSupport {

    @Autowired
    private SmppConnectionPool pool;
    @Autowired
    private SmppReceiverHelper smppReceiverHelper;
    @Value("${smscenter.receiver.smpp.port}")
    private int serverPort;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SenderMapper senderMapper;
    private User user;
    private Sender sender;
    private Connection smppCon;
    private AtomicInteger elCount = new AtomicInteger(0);
    private AtomicInteger elRespCount = new AtomicInteger(0);
    private AtomicInteger ubdCount = new AtomicInteger(0);

    @Before
    public void setUp() throws Exception {
        user = new User();
        user.setName("testUser");
        user.setPassword("testPass");
        user.setEnabled(true);
        userMapper.insert(user);

        sender = new Sender();
        sender.setSign("testSender");
        senderMapper.insert(sender);

        senderMapper.createLink(user, sender);

        smppCon = new Connection("localhost", serverPort, true);
        smppCon.addObserver(new SMPPEventAdapter() {
            @Override
            public void queryLink(Connection source, EnquireLink el) {
                assertNotNull(el);
                elCount.incrementAndGet();
            }

            @Override
            public void queryLinkResponse(Connection source, EnquireLinkResp elr) {
                assertNotNull(elr);
                elRespCount.incrementAndGet();
            }

            @Override
            public void unbind(Connection source, Unbind ubd) {
                assertNotNull(ubd);
                ubdCount.incrementAndGet();
            }
        });
        smppCon.autoAckLink(true);
        smppCon.autoAckMessages(true);
        smppCon.setVersion(SMPPVersion.V34);
        smppCon.bind(Connection.TRANSMITTER, user.getName(), user.getPassword(), null);

        int count = 0;
        while (!(smppCon.isBound()) && (count < 10)) {
            Thread.sleep(1000);
            count++;
        }

        assertTrue(smppCon.isBound());
    }

    @After
    public void tearDown() throws Exception {
        senderMapper.removeLinks(sender);
        userMapper.delete(user);
        senderMapper.delete(sender);

        if (!CollectionUtils.isEmpty(pool.getConnectionHandlers())) {
            pool.closeWithNotification(pool.getConnectionHandlers().get(0));
            int count = 0;
            while ((ubdCount.get() == 0) && (count < 10)) {
                Thread.sleep(1000);
                count++;
            }
            assertEquals(1, ubdCount.get());
        }
    }

    @Test
    public void testHandler() throws Exception {
        SmppConnectionHandler smppConnectionHandler = pool.getConnectionHandlers().get(0);
        EnquireLink req = (EnquireLink) smppReceiverHelper.newInstance(SMPPPacket.ENQUIRE_LINK);
        smppConnectionHandler.sendRequest(req);

        int count = 0;
        while ((elCount.get() == 0) && (count < 10)) {
            Thread.sleep(1000);
            count++;
        }
        assertEquals(1, elCount.get());

        try {
            BindTransmitter bindReq = (BindTransmitter) smppReceiverHelper.newInstance(SMPPPacket.BIND_TRANSMITTER);
            smppConnectionHandler.sendRequest(bindReq);
            fail();
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }

        try {
            SubmitSM submitReq = (SubmitSM) smppReceiverHelper.newInstance(SMPPPacket.SUBMIT_SM);
            smppConnectionHandler.sendRequest(submitReq);
            fail();
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }

        EnquireLinkResp response = (EnquireLinkResp) smppReceiverHelper.newInstance(SMPPPacket.ENQUIRE_LINK_RESP);
        smppConnectionHandler.sendResponse(response);

        count = 0;
        while ((elRespCount.get() == 0) && (count < 10)) {
            Thread.sleep(1000);
            count++;
        }
        assertEquals(1, elRespCount.get());
    }

}
