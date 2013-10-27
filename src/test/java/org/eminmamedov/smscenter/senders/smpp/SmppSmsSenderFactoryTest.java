package org.eminmamedov.smscenter.senders.smpp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eminmamedov.smscenter.common.WorkerInitializer;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.eminmamedov.smscenter.senders.SmsSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmppSmsSenderFactoryTest {

    @Mock
    private WorkerInitializer workerInitializer;
    @InjectMocks
    private SmppSmsSenderFactory factory;

    @Test
    public void testCreateSmsSender() {
        SmsSender smsSender = factory.createSmsSender(new Channel());
        assertNotNull(smsSender);
        assertTrue(smsSender instanceof SmppSmsSender);
        verify(workerInitializer, times(1)).autowire(same(smsSender));
    }

    @Test
    public void testIsApplicable() {
        assertTrue(factory.isApplicable(new Channel()));
    }

}
