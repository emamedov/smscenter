package org.eminmamedov.smscenter.receivers.smpp.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionPool;
import org.eminmamedov.smscenter.receivers.smpp.SmppReceiverHelper;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class DeliverSMSenderTaskTest {

    @Mock
    private SmppConnectionPool smppConnectionPool;
    @Mock
    private SmppReceiverHelper smppReceiverHelper;
    @Mock
    private SmsCenterService smsCenterService;
    @Mock
    private SmppConnectionHandler mockedHandler;
    private DeliverSMSenderTask deliverSMSenderTask;

    @Before
    public void setUp() {
        deliverSMSenderTask = new DeliverSMSenderTask();
        deliverSMSenderTask.setSmppConnectionPool(smppConnectionPool);
        deliverSMSenderTask.setSmppReceiverHelper(smppReceiverHelper);
        deliverSMSenderTask.setSmsCenterService(smsCenterService);
        when(mockedHandler.isNotBound()).thenReturn(false);
        when(mockedHandler.getUser()).thenReturn(new User());
    }

    @Test
    public void testSendRequests_NoHandlers() throws BadCommandIDException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.<SmppConnectionHandler> emptyList());
        deliverSMSenderTask.sendRequests();
        verify(smppReceiverHelper, times(0)).newInstance(anyInt());
        verify(smppConnectionPool, times(0)).close(any(SmppConnectionHandler.class));
    }

    @Test
    public void testSendEnquireLinks_BoundHandler_NoMessages() throws BadCommandIDException, IOException {
        when(smsCenterService.getUpdatedMessages(any(User.class), anyInt())).thenReturn(
                Collections.<SMSMessage> emptyList());

        deliverSMSenderTask.sendRequests();
        verify(smppReceiverHelper, times(0)).newInstance(eq(SMPPPacket.DELIVER_SM));
        verify(smppConnectionPool, times(0)).close(same(mockedHandler));
        verify(mockedHandler, times(0)).sendRequest(any(DeliverSM.class));
    }

    @Test
    public void testSendRequests_NotBoundHandler() throws BadCommandIDException, IOException {
        SmppConnectionHandler handler = new SmppConnectionHandler(new Socket() {
            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return null;
            }
        });
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(handler));

        deliverSMSenderTask.sendRequests();
        verify(smppReceiverHelper, times(0)).newInstance(anyInt());
        verify(smppConnectionPool, times(0)).close(any(SmppConnectionHandler.class));
    }

    @Test
    public void testSendRequests_BoundHandler_UnsupportedOperationException() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.DELIVER_SM))).thenReturn(new DeliverSM());
        SMSMessage message = new SMSMessage();
        message.setId(100L);
        message.setSendDate(new Date());
        message.setStatus(MessageStatus.NEW_MESSAGE_STATE);
        when(smsCenterService.getUpdatedMessages(any(User.class), anyInt())).thenReturn(
                Collections.singletonList(message));
        doThrow(new UnsupportedOperationException()).when(mockedHandler).sendRequest(any(SMPPRequest.class));

        deliverSMSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.DELIVER_SM));
        verify(smppConnectionPool, times(1)).close(same(mockedHandler));
        verify(mockedHandler, times(1)).sendRequest(any(DeliverSM.class));
    }

    @Test
    public void testSendRequests_BoundHandler_BadCommandIDException() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.DELIVER_SM))).thenThrow(new BadCommandIDException());
        SMSMessage message = new SMSMessage();
        message.setId(100L);
        message.setSendDate(new Date());
        message.setStatus(MessageStatus.NEW_MESSAGE_STATE);
        when(smsCenterService.getUpdatedMessages(any(User.class), anyInt())).thenReturn(
                Collections.singletonList(message));

        deliverSMSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.DELIVER_SM));
        verify(smppConnectionPool, times(1)).close(same(mockedHandler));
        verify(mockedHandler, times(0)).sendRequest(any(DeliverSM.class));
    }

    @Test
    public void testSendRequests_BoundHandler_IOException() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.DELIVER_SM))).thenReturn(new DeliverSM());
        doThrow(new IOException()).when(mockedHandler).sendRequest(any(SMPPRequest.class));
        SMSMessage message = new SMSMessage();
        message.setId(100L);
        message.setSendDate(new Date());
        message.setStatus(MessageStatus.NEW_MESSAGE_STATE);
        when(smsCenterService.getUpdatedMessages(any(User.class), anyInt())).thenReturn(
                Collections.singletonList(message));

        deliverSMSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.DELIVER_SM));
        verify(smppConnectionPool, times(1)).close(same(mockedHandler));
        verify(mockedHandler, times(1)).sendRequest(any(DeliverSM.class));
    }

    @Test
    public void testSendRequests_BoundHandler_AnyOtherException() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.DELIVER_SM))).thenReturn(new DeliverSM());
        doThrow(new NullPointerException()).when(mockedHandler).sendRequest(any(SMPPRequest.class));
        SMSMessage message = new SMSMessage();
        message.setId(100L);
        message.setSendDate(new Date());
        message.setStatus(MessageStatus.MESSAGE_WAS_DELIVERED);
        when(smsCenterService.getUpdatedMessages(any(User.class), anyInt())).thenReturn(
                Collections.singletonList(message));

        deliverSMSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.DELIVER_SM));
        verify(smppConnectionPool, times(1)).close(same(mockedHandler));
        verify(mockedHandler, times(1)).sendRequest(any(DeliverSM.class));
    }

    @Test
    public void testSendRequests_Ok() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.DELIVER_SM))).thenReturn(new DeliverSM());
        final SMSMessage message = new SMSMessage();
        message.setId(100L);
        message.setSendDate(new GregorianCalendar(2012, 8, 28, 11, 00, 15).getTime());
        message.setStatus(MessageStatus.NEW_MESSAGE_STATE);
        message.setText("TEST");
        when(smsCenterService.getUpdatedMessages(any(User.class), anyInt())).thenReturn(
                Collections.singletonList(message));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                DeliverSM packet = (DeliverSM) invocation.getArguments()[0];
                assertEquals(100, packet.getSequenceNum());
                assertEquals(
                        "id:0000000100 sub:000 dlvrd:000 submit date:1209281100 done date:1209281100 stat:UNDELIV err:000 Text:TEST",
                        packet.getMessageText());
                return null;
            }
        }).when(mockedHandler).sendRequest(any(SMPPRequest.class));

        deliverSMSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.DELIVER_SM));
        verify(smppConnectionPool, times(0)).close(same(mockedHandler));
        verify(mockedHandler, times(1)).sendRequest(any(DeliverSM.class));
    }

}
