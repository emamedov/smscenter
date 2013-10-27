package org.eminmamedov.smscenter.receivers.smpp.processors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.EnquireLinkResp;
import ie.omk.smpp.message.SubmitSM;

import java.io.IOException;

import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.junit.Before;
import org.junit.Test;

public class EnquireLinkProcessorTest {

    private EnquireLinkProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new EnquireLinkProcessor();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessRequest_WrongPacket() throws IOException {
        processor.processRequest(mock(SmppConnectionHandler.class), mock(SubmitSM.class));
    }

    @Test
    public void testProcessRequest_Ok() throws IOException {
        SmppConnectionHandler handler = mock(SmppConnectionHandler.class);
        when(handler.getUser()).thenReturn(new User());
        EnquireLink link = mock(EnquireLink.class);
        processor.processRequest(handler, link);
        verify(handler, times(1)).resetEnquireLinkCount();
        verify(handler, times(1)).sendResponse(any(EnquireLinkResp.class));
    }

}
