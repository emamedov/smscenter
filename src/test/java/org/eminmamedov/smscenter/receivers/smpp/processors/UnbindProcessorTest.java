package org.eminmamedov.smscenter.receivers.smpp.processors;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.Unbind;

import java.io.IOException;

import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnbindProcessorTest {

    @Mock
    private SmppConnectionPool smppConnectionPool;
    private UnbindProcessor processor;

    @Before
    public void setUp() {
        processor = new UnbindProcessor();
        processor.setSmppConnectionPool(smppConnectionPool);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessRequest_WrongPacket() throws IOException {
        SmppConnectionHandler handler = mock(SmppConnectionHandler.class);
        when(handler.getUser()).thenReturn(new User());
        processor.processRequest(handler, mock(SubmitSM.class));
    }

    @Test
    public void testProcessRequest_Ok() throws IOException {
        SmppConnectionHandler handler = mock(SmppConnectionHandler.class);
        when(handler.getUser()).thenReturn(new User());
        Unbind packet = mock(Unbind.class);
        processor.processRequest(handler, packet);
        verify(smppConnectionPool, times(1)).close(same(handler));
    }

}
