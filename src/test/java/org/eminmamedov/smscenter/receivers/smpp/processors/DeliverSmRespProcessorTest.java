package org.eminmamedov.smscenter.receivers.smpp.processors;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import ie.omk.smpp.message.DeliverSM;

import java.io.IOException;

import org.eminmamedov.smscenter.receivers.smpp.SmppConnectionHandler;
import org.eminmamedov.smscenter.services.SmsCenterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeliverSmRespProcessorTest {

    @Mock
    private SmsCenterService smsCenterService;
    @Mock
    private SmppConnectionHandler handler;
    private DeliverSmRespProcessor processor;

    @Before
    public void setUp() {
        processor = new DeliverSmRespProcessor();
        processor.setSmsCenterService(smsCenterService);
    }

    @Test
    public void testProcessRequest() throws IOException {
        DeliverSM request = new DeliverSM();
        request.setSequenceNum(100);
        processor.processRequest(handler, request);
        verify(smsCenterService, times(1)).setInformedFlagForMessage(eq(100L), eq(true));
        verify(handler, times(1)).resetEnquireLinkCount();
    }

}
