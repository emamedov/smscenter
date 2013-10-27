package org.eminmamedov.smscenter.receivers.smpp.processors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.QuerySM;
import ie.omk.smpp.message.QuerySMResp;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPResponse;

import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppReceiverErrorCodes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class QuerySmProcessorTest {

    @Mock
    private SmsMapper smsMapper;
    @Mock
    private SmppConnectionHandler handler;
    @InjectMocks
    private QuerySmProcessor processor = new QuerySmProcessor();

    @Before
    public void setUp() {
        when(handler.getUser()).thenReturn(new User());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessRequest_InvalidRequestType() throws Exception {
        processor.processRequest(null, new EnquireLink());
    }

    @Test
    public void testProcessRequest_MessageIdEmpty() throws Exception {
        QuerySM request = new QuerySM();
        request.setMessageId("    ");
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                QuerySMResp resp = (QuerySMResp) invocation.getArguments()[0];
                assertEquals(SmppReceiverErrorCodes.ESME_RINVMSGID, resp.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SMPPResponse.class));

        processor.processRequest(handler, request);

        verify(handler, times(1)).sendResponse(any(QuerySMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
    }

    @Test
    public void testProcessRequest_MessageIdIsNotDigit() throws Exception {
        QuerySM request = new QuerySM();
        request.setMessageId("123A");
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                QuerySMResp resp = (QuerySMResp) invocation.getArguments()[0];
                assertEquals(SmppReceiverErrorCodes.ESME_RINVMSGID, resp.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SMPPResponse.class));

        processor.processRequest(handler, request);

        verify(handler, times(1)).sendResponse(any(QuerySMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
    }

    @Test
    public void testProcessRequest_MessageNotFound() throws Exception {
        QuerySM request = new QuerySM();
        request.setMessageId("123");
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                QuerySMResp resp = (QuerySMResp) invocation.getArguments()[0];
                assertEquals(SmppReceiverErrorCodes.ESME_RINVMSGID, resp.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SMPPResponse.class));

        processor.processRequest(handler, request);

        verify(handler, times(1)).sendResponse(any(QuerySMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
    }

    @Test
    public void testProcessRequest_MessageFound() throws Exception {
        QuerySM request = new QuerySM();
        request.setMessageId("123");
        SMSMessage smsMessage = new SMSMessage();
        smsMessage.setStatus(MessageStatus.NEW_MESSAGE_STATE);
        when(smsMapper.getMessage(eq(123L))).thenReturn(smsMessage);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                QuerySMResp resp = (QuerySMResp) invocation.getArguments()[0];
                assertEquals(0, resp.getCommandStatus());
                assertEquals(SMPPPacket.SM_STATE_EN_ROUTE, resp.getMessageStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SMPPResponse.class));

        processor.processRequest(handler, request);

        verify(handler, times(1)).sendResponse(any(QuerySMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
    }

}
