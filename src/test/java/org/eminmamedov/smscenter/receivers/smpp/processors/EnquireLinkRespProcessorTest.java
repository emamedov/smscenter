package org.eminmamedov.smscenter.receivers.smpp.processors;

import static org.mockito.Mockito.*;

import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.junit.Before;
import org.junit.Test;

public class EnquireLinkRespProcessorTest {

    private EnquireLinkRespProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new EnquireLinkRespProcessor();
    }

    @Test
    public void testProcessRequest() {
        SmppConnectionHandler handler = mock(SmppConnectionHandler.class);
        when(handler.getUser()).thenReturn(new User());
        processor.processRequest(handler, null);
        verify(handler, times(1)).resetEnquireLinkCount();
    }

}
