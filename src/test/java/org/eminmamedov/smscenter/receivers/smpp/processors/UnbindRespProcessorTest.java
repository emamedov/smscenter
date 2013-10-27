package org.eminmamedov.smscenter.receivers.smpp.processors;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnbindRespProcessorTest {

    @Mock
    private SmppConnectionPool smppConnectionPool;
    private UnbindRespProcessor processor;

    @Before
    public void setUp() {
        processor = new UnbindRespProcessor();
        processor.setSmppConnectionPool(smppConnectionPool);
    }

    @Test
    public void testProcessRequest() {
        SmppConnectionHandler handler = mock(SmppConnectionHandler.class);
        when(handler.getUser()).thenReturn(new User());
        processor.processRequest(handler, null);
        verify(smppConnectionPool, times(1)).close(same(handler));
    }

}
