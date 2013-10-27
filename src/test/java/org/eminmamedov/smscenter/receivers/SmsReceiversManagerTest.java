package org.eminmamedov.smscenter.receivers;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class SmsReceiversManagerTest {

    private SmsReceiversManager manager;

    @Before
    public void setUp() throws Exception {
        manager = new SmsReceiversManager();
    }

    @Test
    public void testStartNullList() {
        manager.setSmsReceivers(null);
        manager.startAll();
    }
    
    @Test
    public void testStartEmptyList() {
        manager.setSmsReceivers(Collections.<SmsReceiver>emptyList());
        manager.startAll();
    }

    @Test
    public void testStartOk() {
        SmsReceiver smsReceiver1 = mock(SmsReceiver.class);
        SmsReceiver smsReceiver2 = mock(SmsReceiver.class);
        manager.setSmsReceivers(Arrays.asList(smsReceiver1, smsReceiver2));
        manager.startAll();
        verify(smsReceiver1).start();
        verify(smsReceiver2).start();
    }

    @Test
    public void testStopNullList() {
        manager.setSmsReceivers(null);
        manager.stopAll();
    }
    
    @Test
    public void testStopEmptyList() {
        manager.setSmsReceivers(Collections.<SmsReceiver>emptyList());
        manager.stopAll();
    }

    @Test
    public void testStopOk() {
        SmsReceiver smsReceiver1 = mock(SmsReceiver.class);
        SmsReceiver smsReceiver2 = mock(SmsReceiver.class);
        manager.setSmsReceivers(Arrays.asList(smsReceiver1, smsReceiver2));
        manager.stopAll();
        verify(smsReceiver1).stop();
        verify(smsReceiver2).stop();
    }

}
