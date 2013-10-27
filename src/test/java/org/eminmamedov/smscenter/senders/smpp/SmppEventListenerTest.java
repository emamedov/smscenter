package org.eminmamedov.smscenter.senders.smpp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import ie.omk.smpp.Connection;
import ie.omk.smpp.SMPPException;
import ie.omk.smpp.SMPPRuntimeException;
import ie.omk.smpp.event.ReceiverExceptionEvent;
import ie.omk.smpp.event.ReceiverExitEvent;
import ie.omk.smpp.message.BindTransceiverResp;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.QuerySMResp;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.message.Unbind;
import ie.omk.smpp.message.UnbindResp;
import ie.omk.smpp.message.tlv.Tag;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.datamodel.MessageStatus;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.senders.SmsSendersManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmppEventListenerTest {

    @Mock
    private SmsSendersManager smsSendersManager;
    @Mock
    private SmsMapper smsMapper;
    @Spy
    private MessageStatusConverter messageStatusConverter;
    @Mock
    private Channel channel;
    @InjectMocks
    private SmppEventListener smppEventListener;

    @Before
    public void setUp() throws Exception {
        Whitebox.setInternalState(smppEventListener, "smsMapper", smsMapper);
        Whitebox.setInternalState(smppEventListener, "messageStatusConverter", messageStatusConverter);
        Whitebox.setInternalState(smppEventListener, "smsSendersManager", smsSendersManager);
        assertSame(channel, smppEventListener.getChannel());
    }

    @Test
    public void testReceiverExit() {
        smppEventListener.receiverExit(null, new ReceiverExitEvent(null));
        verify(smsSendersManager, times(1)).closeSender(same(channel));
    }

    @Test
    public void testReceiverExitException() {
        smppEventListener.receiverExitException(null, new ReceiverExitEvent(null));
        verify(smsSendersManager, times(1)).closeSender(same(channel));
    }

    @Test
    public void testReceiverException() {
        smppEventListener.receiverException(null, new ReceiverExceptionEvent(null, null));
        verify(smsSendersManager, times(0)).closeSender(same(channel));
    }

    @Test
    public void testDeliverSM_MessageTextBlank() {
        DeliverSM dm = new DeliverSM();
        smppEventListener.deliverSM(null, dm);
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testDeliverSM_StateObjNull_WrongMessage() {
        DeliverSM dm = new DeliverSM();
        dm.setMessageText("WRONG MESSAGE TEXT");
        smppEventListener.deliverSM(null, dm);
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testDeliverSM_SmscIdObjNull_MessageNotFound() {
        DeliverSM dm = new DeliverSM();
        dm.setOptionalParameter(Tag.MESSAGE_STATE, SMPPPacket.SM_STATE_DELIVERED);
        dm.setMessageText("id:0000100 Text:BlaBla stat:DELIVRD");
        smppEventListener.deliverSM(null, dm);
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testDeliverSM_SmscIdObjNull_Found() {
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessageBySmscId(any(Channel.class), eq("100"))).thenReturn(message);

        DeliverSM dm = new DeliverSM();
        dm.setOptionalParameter(Tag.MESSAGE_STATE, SMPPPacket.SM_STATE_DELIVERED);
        dm.setMessageText("id:0000100 Text:BlaBla stat:DELIVRD");
        smppEventListener.deliverSM(null, dm);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.MESSAGE_WAS_DELIVERED, message.getStatus());
    }

    @Test
    public void testDeliverSM_OptionalParamsNotNull() {
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessageBySmscId(any(Channel.class), eq("100"))).thenReturn(message);

        DeliverSM dm = new DeliverSM();
        dm.setOptionalParameter(Tag.MESSAGE_STATE, SMPPPacket.SM_STATE_EXPIRED);
        dm.setOptionalParameter(Tag.RECEIPTED_MESSAGE_ID, "000100");
        dm.setMessageText("id:0000100 Text:BlaBla");
        smppEventListener.deliverSM(null, dm);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, message.getStatus());
    }

    @Test
    public void testBindResponse_Ok() {
        BindTransceiverResp br = new BindTransceiverResp();
        br.setCommandStatus(0);
        smppEventListener.bindResponse(null, br);
        verify(smsSendersManager, times(0)).closeSender(same(channel));
    }

    @Test
    public void testBindResponse_NotOk() {
        BindTransceiverResp br = new BindTransceiverResp();
        br.setCommandStatus(1);
        smppEventListener.bindResponse(null, br);
        verify(smsSendersManager, times(1)).closeSender(same(channel));
    }

    @Test
    public void testUnbind_Ok() throws Exception {
        Unbind ubd = new Unbind();
        Connection source = mock(Connection.class);
        smppEventListener.unbind(source, ubd);
        verify(source, times(1)).unbind(any(UnbindResp.class));
        verify(smsSendersManager, times(1)).closeSender(same(channel));
    }

    @Test
    public void testUnbind_SMPPRuntimeException() throws Exception {
        Unbind ubd = new Unbind();
        Connection source = mock(Connection.class);
        doThrow(new SMPPRuntimeException()).when(source).unbind(any(UnbindResp.class));
        smppEventListener.unbind(source, ubd);
        verify(source, times(1)).unbind(any(UnbindResp.class));
        verify(smsSendersManager, times(1)).closeSender(same(channel));
    }

    @Test
    public void testUnbind_IOException() throws Exception {
        Unbind ubd = new Unbind();
        Connection source = mock(Connection.class);
        doThrow(new IOException()).when(source).unbind(any(UnbindResp.class));
        smppEventListener.unbind(source, ubd);
        verify(source, times(1)).unbind(any(UnbindResp.class));
        verify(smsSendersManager, times(1)).closeSender(same(channel));
    }

    @Test
    public void testUnbind_SMPPException() throws Exception {
        Unbind ubd = new Unbind();
        Connection source = mock(Connection.class);
        doThrow(new SMPPException()).when(source).unbind(any(UnbindResp.class));
        smppEventListener.unbind(source, ubd);
        verify(source, times(1)).unbind(any(UnbindResp.class));
        verify(smsSendersManager, times(1)).closeSender(same(channel));
    }

    @Test
    public void testUnbindResponse() {
        smppEventListener.unbindResponse(null, new UnbindResp());
        verify(smsSendersManager, times(1)).closeSender(same(channel));
    }

    @Test
    public void testUnidentified() {
        smppEventListener.unidentified(null, null);
        verify(smsSendersManager, times(0)).closeSender(same(channel));
    }

    @Test
    public void testSubmitSM_MessageNotFound() {
        int sequenceNum = 100;
        when(smsMapper.getMessage(eq(Long.valueOf(sequenceNum)))).thenReturn(null);

        SubmitSMResp smr = new SubmitSMResp();
        smr.setMessageId(null);
        smr.setSequenceNum(sequenceNum);
        smppEventListener.submitSMResponse(null, smr);
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testSubmitSM_WrongSequenceNum() {
        int sequenceNum = -100;

        SubmitSMResp smr = new SubmitSMResp();
        smr.setMessageId(null);
        smr.setSequenceNum(sequenceNum);
        smppEventListener.submitSMResponse(null, smr);
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testSubmitSM_InQueue() {
        String id = "123";
        int sequenceNum = 100;
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessage(eq(Long.valueOf(sequenceNum)))).thenReturn(message);

        SubmitSMResp smr = new SubmitSMResp();
        smr.setMessageId(id);
        smr.setSequenceNum(sequenceNum);
        smppEventListener.submitSMResponse(null, smr);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.MESSAGE_IN_QUEUE, message.getStatus());
        assertEquals("123", message.getSmscId());
    }

    @Test
    public void testSubmitSM_Trottling() {
        String id = null;
        int sequenceNum = 100;
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessage(eq(Long.valueOf(sequenceNum)))).thenReturn(message);

        SubmitSMResp smr = new SubmitSMResp();
        smr.setMessageId(id);
        smr.setCommandStatus(0x58);
        smr.setSequenceNum(sequenceNum);
        smppEventListener.submitSMResponse(null, smr);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.NEW_MESSAGE_STATE, message.getStatus());
        assertNull(message.getSmscId());
    }

    @Test
    public void testSubmitSM_NotDelivered() {
        String id = null;
        int sequenceNum = 100;
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessage(eq(Long.valueOf(sequenceNum)))).thenReturn(message);

        SubmitSMResp smr = new SubmitSMResp();
        smr.setMessageId(id);
        smr.setSequenceNum(sequenceNum);
        smppEventListener.submitSMResponse(null, smr);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, message.getStatus());
        assertNull(message.getSmscId());
    }

    @Test
    public void testQueryResponse_MessageIdNull() {
        QuerySMResp qr = new QuerySMResp();
        smppEventListener.queryResponse(null, qr);
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testQueryResponse_MessageNotFound() {
        QuerySMResp qr = new QuerySMResp();
        qr.setMessageId("100");
        when(smsMapper.getMessageBySmscId(any(Channel.class), eq("100"))).thenReturn(null);

        smppEventListener.queryResponse(null, qr);
        verify(smsMapper, times(0)).update(any(SMSMessage.class));
    }

    @Test
    public void testQueryResponse_DELIVERED() {
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessageBySmscId(any(Channel.class), eq("100"))).thenReturn(message);

        QuerySMResp qr = new QuerySMResp();
        qr.setMessageId("100");
        qr.setMessageStatus(SMPPPacket.SM_STATE_DELIVERED);

        smppEventListener.queryResponse(null, qr);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.MESSAGE_WAS_DELIVERED, message.getStatus());
    }

    @Test
    public void testQueryResponse_UNDELIVERABLE() {
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessageBySmscId(any(Channel.class), eq("100"))).thenReturn(message);

        QuerySMResp qr = new QuerySMResp();
        qr.setMessageId("100");
        qr.setMessageStatus(SMPPPacket.SM_STATE_UNDELIVERABLE);

        smppEventListener.queryResponse(null, qr);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, message.getStatus());
    }

    @Test
    public void testQueryResponse_EN_ROUTE_ShouldWait() {
        SMSMessage message = new SMSMessage();
        message.setSendDate(new Date());
        when(smsMapper.getMessageBySmscId(any(Channel.class), eq("100"))).thenReturn(message);

        QuerySMResp qr = new QuerySMResp();
        qr.setMessageId("100");
        qr.setMessageStatus(SMPPPacket.SM_STATE_EN_ROUTE);

        smppEventListener.queryResponse(null, qr);
        verify(smsMapper, times(1)).update(same(message));
        assertNull(message.getStatus());
    }

    @Test
    public void testQueryResponse_EN_ROUTE_ShouldMarkMessageAsUndeliverable() {
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessageBySmscId(any(Channel.class), eq("100"))).thenReturn(message);

        QuerySMResp qr = new QuerySMResp();
        qr.setMessageId("100");
        qr.setMessageStatus(SMPPPacket.SM_STATE_EN_ROUTE);
        qr.setFinalDate(new GregorianCalendar(2010, 10, 10).getTime());

        smppEventListener.queryResponse(null, qr);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, message.getStatus());
    }

    @Test
    public void testQueryResponse_OTHER() {
        SMSMessage message = new SMSMessage();
        when(smsMapper.getMessageBySmscId(any(Channel.class), eq("100"))).thenReturn(message);

        QuerySMResp qr = new QuerySMResp();
        qr.setMessageId("100");
        qr.setMessageStatus(SMPPPacket.SM_STATE_EXPIRED);

        smppEventListener.queryResponse(null, qr);
        verify(smsMapper, times(1)).update(same(message));
        assertEquals(MessageStatus.MESSAGE_WASNT_DELIVERED, message.getStatus());
    }

}
