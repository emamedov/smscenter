package org.eminmamedov.smscenter.receivers.smpp.processors;

import static junit.framework.Assert.assertEquals;
import static org.eminmamedov.smscenter.receivers.smpp.BoundState.*;
import static org.eminmamedov.smscenter.receivers.smpp.SmppConnectionType.RECEIVER;
import static org.eminmamedov.smscenter.receivers.smpp.SmppReceiverErrorCodes.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import ie.omk.smpp.message.Bind;
import ie.omk.smpp.message.BindReceiver;
import ie.omk.smpp.message.BindReceiverResp;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.BindTransceiver;
import ie.omk.smpp.message.BindTransceiverResp;
import ie.omk.smpp.message.BindTransmitter;
import ie.omk.smpp.message.BindTransmitterResp;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SMPPResponse;
import ie.omk.smpp.version.SMPPVersion;

import java.io.IOException;
import java.net.Socket;

import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.exceptions.NotFoundException;
import org.eminmamedov.smscenter.receivers.smpp.BoundState;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionPool;
import org.eminmamedov.smscenter.receivers.smpp.SmsClientLink;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class BindProcessorTest {

    @Mock
    private SmsCenterService smsCenterService;
    @Mock
    private SmppConnectionHandler handler;
    @Mock
    private SmppConnectionPool smppConnectionPool;
    private BindProcessor bindProcessor;

    @Before
    public void setUp() {
        bindProcessor = new BindProcessor();
        bindProcessor.setSmsCenterService(smsCenterService);
        bindProcessor.setSmppConnectionPool(smppConnectionPool);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessRequest_WrongRequestType() throws IOException {
        bindProcessor.processRequest(handler, new DeliverSM());
        verify(handler, times(0)).setBoundState(any(BoundState.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessRequest_WrongCommandId() throws IOException {
        bindProcessor.processRequest(handler, new Bind(3) {
        });
        verify(handler, times(0)).setBoundState(any(BoundState.class));
    }

    @Test
    public void testProcessRequest_AlreadyBound() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BindResp response = (BindResp) invocation.getArguments()[0];
                assertEquals(ESME_RALYBND, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(BindTransmitterResp.class));

        when(handler.getBoundState()).thenReturn(BOUND);
        bindProcessor.processRequest(handler, new BindTransmitter());
        verify(handler, times(1)).sendResponse(any(SMPPResponse.class));
        verify(handler, times(0)).setBoundState(any(BoundState.class));
    }

    @Test
    public void testProcessRequest_InvalidBoundState() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BindResp response = (BindResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVBNDSTS, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(BindTransceiverResp.class));

        when(handler.getBoundState()).thenReturn(UNBOUNDING);
        bindProcessor.processRequest(handler, new BindTransceiver());
        verify(handler, times(1)).sendResponse(any(SMPPResponse.class));
        verify(handler, times(0)).setBoundState(any(BoundState.class));
    }

    @Test
    public void testProcessRequest_WrongSmppVersion() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BindResp response = (BindResp) invocation.getArguments()[0];
                assertEquals(ESME_RSYSERR, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(BindReceiverResp.class));

        when(handler.getBoundState()).thenReturn(BINDING);
        BindReceiver request = new BindReceiver();
        request.setVersion(SMPPVersion.V33);
        bindProcessor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SMPPResponse.class));
        verify(handler, times(0)).setBoundState(any(BoundState.class));
    }

    @Test
    public void testProcessRequest_UserNotFound() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BindResp response = (BindResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVPASWD, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(BindReceiverResp.class));

        when(handler.getBoundState()).thenReturn(BINDING);
        String login = "testUser";
        String password = "testPass";
        when(smsCenterService.getUser(eq(login), eq(password), anyString())).thenThrow(new NotFoundException(""));
        SmsClientLink link = mock(SmsClientLink.class);
        when(link.getClientSocket()).thenReturn(new Socket());
        when(handler.getLink()).thenReturn(link);

        BindReceiver request = new BindReceiver();
        request.setSystemId(login);
        request.setPassword(password);
        request.setVersion(SMPPVersion.V34);
        bindProcessor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SMPPResponse.class));
        verify(handler, times(1)).setBoundState(eq(BoundState.UNBOUND));
    }

    @Test
    public void testProcessRequest_ExceptionOccured() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BindResp response = (BindResp) invocation.getArguments()[0];
                assertEquals(ESME_RSYSERR, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(BindReceiverResp.class));

        when(handler.getBoundState()).thenReturn(BINDING);
        String login = "testUser";
        String password = "testPass";
        when(smsCenterService.getUser(eq(login), eq(password), anyString())).thenThrow(new NullPointerException());
        SmsClientLink link = mock(SmsClientLink.class);
        when(link.getClientSocket()).thenReturn(new Socket());
        when(handler.getLink()).thenReturn(link);

        BindReceiver request = new BindReceiver();
        request.setSystemId(login);
        request.setPassword(password);
        request.setVersion(SMPPVersion.V34);
        bindProcessor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SMPPResponse.class));
        verify(handler, times(1)).setBoundState(eq(BoundState.UNBOUND));
    }

    @Test
    public void testProcessRequest_HandlerExistAlready() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BindResp response = (BindResp) invocation.getArguments()[0];
                assertEquals(ESME_RALYBND, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(BindReceiverResp.class));

        when(handler.getBoundState()).thenReturn(BINDING);
        String login = "testUser";
        String password = "testPass";
        when(smsCenterService.getUser(eq(login), eq(password), anyString())).thenReturn(new User());
        SmsClientLink link = mock(SmsClientLink.class);
        when(link.getClientSocket()).thenReturn(new Socket());
        when(handler.getLink()).thenReturn(link);
        when(smppConnectionPool.handlerExistsAlready(any(User.class))).thenReturn(true);

        BindReceiver request = new BindReceiver();
        request.setSystemId(login);
        request.setPassword(password);
        request.setVersion(SMPPVersion.V34);
        bindProcessor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SMPPResponse.class));
        verify(handler, times(1)).setBoundState(eq(BoundState.UNBOUND));
    }

    @Test
    public void testProcessRequest_UserIsLocked() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BindResp response = (BindResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVPASWD, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(BindReceiverResp.class));

        when(handler.getBoundState()).thenReturn(BINDING);
        String login = "testUser";
        String password = "testPass";
        User user = new User();
        user.setEnabled(false);
        when(smsCenterService.getUser(eq(login), eq(password), anyString())).thenReturn(user);
        SmsClientLink link = mock(SmsClientLink.class);
        when(link.getClientSocket()).thenReturn(new Socket());
        when(handler.getLink()).thenReturn(link);
        when(smppConnectionPool.handlerExistsAlready(any(User.class))).thenReturn(false);

        BindReceiver request = new BindReceiver();
        request.setSystemId(login);
        request.setPassword(password);
        request.setVersion(SMPPVersion.V34);
        bindProcessor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SMPPResponse.class));
        verify(handler, times(1)).setBoundState(eq(BoundState.UNBOUND));
    }

    @Test
    public void testProcessRequest_Ok() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BindResp response = (BindResp) invocation.getArguments()[0];
                assertEquals(0, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(BindReceiverResp.class));

        when(handler.getBoundState()).thenReturn(BINDING);
        String login = "testUser";
        String password = "testPass";
        User user = new User();
        user.setEnabled(true);
        when(smsCenterService.getUser(eq(login), eq(password), anyString())).thenReturn(user);
        SmsClientLink link = mock(SmsClientLink.class);
        when(link.getClientSocket()).thenReturn(new Socket());
        when(handler.getLink()).thenReturn(link);
        when(smppConnectionPool.handlerExistsAlready(any(User.class))).thenReturn(false);

        BindReceiver request = new BindReceiver();
        request.setSystemId(login);
        request.setPassword(password);
        request.setVersion(SMPPVersion.V34);
        bindProcessor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SMPPResponse.class));
        verify(handler, times(1)).setBoundState(any(BoundState.class));
        verify(handler, times(1)).setBoundState(eq(BOUND));
        verify(handler, times(1)).setConnectionType(eq(RECEIVER));
        verify(handler, times(1)).setUser(same(user));
    }

}
