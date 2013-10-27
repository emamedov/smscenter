package org.eminmamedov.smscenter.receivers.smpp.tasks;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;

import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionPool;
import org.eminmamedov.smscenter.receivers.smpp.SmppReceiverHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnquireLinkSenderTaskTest {

    @Mock
    private SmppConnectionPool smppConnectionPool;
    @Mock
    private SmppReceiverHelper smppReceiverHelper;
    @Mock
    private SmppConnectionHandler mockedHandler;
    private EnquireLinkSenderTask enquireLinkSenderTask;

    @Before
    public void setUp() {
        enquireLinkSenderTask = new EnquireLinkSenderTask();
        enquireLinkSenderTask.setSmppConnectionPool(smppConnectionPool);
        enquireLinkSenderTask.setSmppReceiverHelper(smppReceiverHelper);

        when(mockedHandler.isNotBound()).thenReturn(false);
        when(mockedHandler.getEnquireLinkCount()).thenReturn(1);

    }

    @Test
    public void testSendRequests_NoHandlers() throws BadCommandIDException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.<SmppConnectionHandler> emptyList());
        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(0)).newInstance(anyInt());
        verify(smppConnectionPool, times(0)).close(any(SmppConnectionHandler.class));
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

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(0)).newInstance(anyInt());
        verify(smppConnectionPool, times(0)).close(any(SmppConnectionHandler.class));

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(0)).newInstance(anyInt());
        verify(smppConnectionPool, times(0)).close(any(SmppConnectionHandler.class));

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(0)).newInstance(anyInt());
        verify(smppConnectionPool, times(0)).close(any(SmppConnectionHandler.class));

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(0)).newInstance(anyInt());
        verify(smppConnectionPool, times(1)).close(same(handler));
    }

    @Test
    public void testSendRequests_BoundHandler_UnsupportedOperationException() throws BadCommandIDException,
            IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.ENQUIRE_LINK))).thenReturn(new EnquireLink());
        doThrow(new UnsupportedOperationException()).when(mockedHandler).sendRequest(any(SMPPRequest.class));

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.ENQUIRE_LINK));
        verify(smppConnectionPool, times(1)).close(same(mockedHandler));
        verify(mockedHandler, times(1)).sendRequest(any(SMPPRequest.class));
    }

    @Test
    public void testSendRequests_BoundHandler_BadCommandIDException() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.ENQUIRE_LINK))).thenThrow(new BadCommandIDException());

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.ENQUIRE_LINK));
        verify(smppConnectionPool, times(1)).close(same(mockedHandler));
        verify(mockedHandler, times(0)).sendRequest(any(SMPPRequest.class));
    }

    @Test
    public void testSendRequests_BoundHandler_IOException() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.ENQUIRE_LINK))).thenReturn(new EnquireLink());
        doThrow(new IOException()).when(mockedHandler).sendRequest(any(SMPPRequest.class));

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.ENQUIRE_LINK));
        verify(smppConnectionPool, times(1)).close(same(mockedHandler));
        verify(mockedHandler, times(1)).sendRequest(any(SMPPRequest.class));
    }

    @Test
    public void testSendRequests_BoundHandler_AnyOtherException() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.ENQUIRE_LINK))).thenReturn(new EnquireLink());
        doThrow(new NullPointerException()).when(mockedHandler).sendRequest(any(SMPPRequest.class));

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.ENQUIRE_LINK));
        verify(smppConnectionPool, times(1)).close(same(mockedHandler));
        verify(mockedHandler, times(1)).sendRequest(any(SMPPRequest.class));
    }

    @Test
    public void testSendRequests_BoundHandler_Ok() throws BadCommandIDException, IOException {
        when(smppConnectionPool.getConnectionHandlers()).thenReturn(Collections.singletonList(mockedHandler));
        when(smppReceiverHelper.newInstance(eq(SMPPPacket.ENQUIRE_LINK))).thenReturn(new EnquireLink());

        enquireLinkSenderTask.sendRequests();
        verify(smppReceiverHelper, times(1)).newInstance(eq(SMPPPacket.ENQUIRE_LINK));
        verify(smppConnectionPool, times(0)).close(same(mockedHandler));
        verify(mockedHandler, times(1)).sendRequest(any(SMPPRequest.class));
    }

}
