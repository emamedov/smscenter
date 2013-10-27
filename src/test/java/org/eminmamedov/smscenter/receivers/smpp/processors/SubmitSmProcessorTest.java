package org.eminmamedov.smscenter.receivers.smpp.processors;

import static junit.framework.Assert.assertEquals;
import static org.eminmamedov.smscenter.receivers.smpp.SmppReceiverErrorCodes.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import ie.omk.smpp.Address;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.message.tlv.Tag;

import java.io.IOException;

import org.eminmamedov.smscenter.common.SmsCenterUtils;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class SubmitSmProcessorTest {

    @Mock
    private SmsCenterService smsCenterService;
    @Mock
    private SmsCenterUtils smsCenterUtils;
    @Mock
    private SmppConnectionHandler handler;
    private SubmitSmProcessor processor;

    @Before
    public void setUp() {
        processor = new SubmitSmProcessor();
        processor.setSmsCenterService(smsCenterService);
        when(smsCenterUtils.checkNumber(anyString())).thenCallRealMethod();
        processor.setSmsCenterUtils(smsCenterUtils);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessRequest_WrongRequestType() throws IOException {
        processor.processRequest(handler, new DeliverSM());
        verify(smsCenterService, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testProcessRequest_NotBound() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SubmitSMResp response = (SubmitSMResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVBNDSTS, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SubmitSMResp.class));
        when(handler.isNotBound()).thenReturn(true);

        processor.processRequest(handler, new SubmitSM());
        verify(handler, times(1)).sendResponse(any(SubmitSMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(smsCenterService, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testProcessRequest_WrongSmsId() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SubmitSMResp response = (SubmitSMResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVMSGID, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SubmitSMResp.class));
        when(handler.isNotBound()).thenReturn(false);

        processor.processRequest(handler, new SubmitSM());
        verify(handler, times(1)).sendResponse(any(SubmitSMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(smsCenterService, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testProcessRequest_WrongPhone() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SubmitSMResp response = (SubmitSMResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVDSTADR, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SubmitSMResp.class));
        when(handler.isNotBound()).thenReturn(false);

        SubmitSM request = new SubmitSM();
        request.setSequenceNum(100);
        request.setDestination(new Address(10, 10, "132534"));
        processor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SubmitSMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(smsCenterService, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testProcessRequest_WrongSender() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SubmitSMResp response = (SubmitSMResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVSRCADR, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SubmitSMResp.class));
        when(handler.isNotBound()).thenReturn(false);
        when(smsCenterService.getSender(any(User.class), anyString())).thenReturn(null);

        SubmitSM request = new SubmitSM();
        request.setSequenceNum(100);
        request.setDestination(new Address(10, 10, "+79853869839"));
        request.setSource(new Address(10, 10, "SENDER"));
        processor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SubmitSMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(smsCenterService, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testProcessRequest_WrongGroupSegmentsParams1() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SubmitSMResp response = (SubmitSMResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVOPTPARAMVAL, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SubmitSMResp.class));
        when(handler.isNotBound()).thenReturn(false);
        when(smsCenterService.getSender(any(User.class), anyString())).thenReturn(createSender("SENDER"));

        SubmitSM request = new SubmitSM();
        request.setSequenceNum(100);
        request.setDestination(new Address(10, 10, "+79853869839"));
        request.setSource(new Address(10, 10, "SENDER"));
        request.setOptionalParameter(Tag.SAR_MSG_REF_NUM, 1);
        request.setOptionalParameter(Tag.SAR_TOTAL_SEGMENTS, 1);
        request.setOptionalParameter(Tag.SAR_SEGMENT_SEQNUM, 2);
        processor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SubmitSMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(smsCenterService, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testProcessRequest_WrongGroupSegmentsParams2() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SubmitSMResp response = (SubmitSMResp) invocation.getArguments()[0];
                assertEquals(ESME_RINVOPTPARAMVAL, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SubmitSMResp.class));
        when(handler.isNotBound()).thenReturn(false);
        when(smsCenterService.getSender(any(User.class), anyString())).thenReturn(createSender("SENDER"));

        SubmitSM request = new SubmitSM();
        request.setSequenceNum(100);
        request.setDestination(new Address(10, 10, "+79853869839"));
        request.setSource(new Address(10, 10, "SENDER"));
        request.setOptionalParameter(Tag.SAR_MSG_REF_NUM, 1);
        request.setOptionalParameter(Tag.SAR_TOTAL_SEGMENTS, 1);
        request.setOptionalParameter(Tag.SAR_SEGMENT_SEQNUM, 0);
        processor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SubmitSMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(smsCenterService, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testProcessRequest_ExceptionOccured() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SubmitSMResp response = (SubmitSMResp) invocation.getArguments()[0];
                assertEquals(ESME_RSYSERR, response.getCommandStatus());
                return null;
            }
        }).when(handler).sendResponse(any(SubmitSMResp.class));
        when(handler.isNotBound()).thenReturn(false);
        when(smsCenterService.getSender(any(User.class), anyString())).thenReturn(createSender("SENDER"));
        doThrow(new NullPointerException()).when(smsCenterService).addMessages(anyListOf(SMSMessage.class));

        SubmitSM request = new SubmitSM();
        request.setSequenceNum(100);
        request.setDestination(new Address(10, 10, "+79853869839"));
        request.setSource(new Address(10, 10, ""));
        request.setOptionalParameter(Tag.SAR_MSG_REF_NUM, 1);
        request.setOptionalParameter(Tag.SAR_TOTAL_SEGMENTS, 1);
        request.setOptionalParameter(Tag.SAR_SEGMENT_SEQNUM, 1);
        processor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SubmitSMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(smsCenterService, times(1)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testProcessRequest_Ok() throws IOException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SubmitSMResp response = (SubmitSMResp) invocation.getArguments()[0];
                assertEquals(0, response.getCommandStatus());
                assertEquals("110", response.getMessageId());
                return null;
            }
        }).when(handler).sendResponse(any(SubmitSMResp.class));
        when(handler.isNotBound()).thenReturn(false);
        when(smsCenterService.getSender(any(User.class), anyString())).thenReturn(createSender("SENDER"));
        when(smsCenterService.reserveServerId()).thenReturn(110L);

        SubmitSM request = new SubmitSM();
        request.setSequenceNum(100);
        request.setDestination(new Address(10, 10, "+79853869839"));
        request.setSource(new Address(10, 10, "SENDER"));
        processor.processRequest(handler, request);
        verify(handler, times(1)).sendResponse(any(SubmitSMResp.class));
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(smsCenterService, times(1)).addMessages(anyListOf(SMSMessage.class));
    }

    private Sender createSender(String senderSign) {
        Sender sender = new Sender();
        sender.setSign(senderSign);
        return sender;
    }

}
