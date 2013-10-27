package org.eminmamedov.smscenter.senders;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eminmamedov.smscenter.dao.ChannelMapper;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmsSendersManagerTest {

    @Mock
    private ChannelMapper channelMapper;
    @InjectMocks
    private SmsSendersManager manager;

    @Before
    public void setUp() {
        List<SmsSenderFactory> smsSendersFactories = new ArrayList<SmsSenderFactory>();
        SmsSenderFactory smsSenderFactory = mock(SmsSenderFactory.class);
        when(smsSenderFactory.isApplicable(any(Channel.class))).thenReturn(true);
        when(smsSenderFactory.createSmsSender(any(Channel.class))).thenReturn(mock(SmsSender.class));
        smsSendersFactories.add(smsSenderFactory);
        Whitebox.setInternalState(manager, "smsSendersFactories", smsSendersFactories);
    }

    @Test
    public void testInitSmsSenders_NoChannels() {
        when(channelMapper.findAll()).thenReturn(Collections.<Channel> emptyList());
        manager.initSmsSenders();
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testInitSmsSenders_ChannelsExist_NoFactories() {
        List<SmsSenderFactory> smsSendersFactories = new ArrayList<SmsSenderFactory>();
        Whitebox.setInternalState(manager, "smsSendersFactories", smsSendersFactories);

        Channel channel1 = createChannel("SENDER1");
        channel1.setEnabled(true);
        Channel channel2 = createChannel("SENDER2");
        channel2.setEnabled(false);
        when(channelMapper.findAll()).thenReturn(Arrays.asList(channel1, channel2));
        manager.initSmsSenders();
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testInitSmsSenders_ChannelsExist_NoApplicableFactories() {
        List<SmsSenderFactory> smsSendersFactories = new ArrayList<SmsSenderFactory>();
        SmsSenderFactory smsSenderFactory = mock(SmsSenderFactory.class);
        when(smsSenderFactory.isApplicable(any(Channel.class))).thenReturn(false);
        when(smsSenderFactory.createSmsSender(any(Channel.class))).thenReturn(mock(SmsSender.class));
        smsSendersFactories.add(smsSenderFactory);
        Whitebox.setInternalState(manager, "smsSendersFactories", smsSendersFactories);

        Channel channel1 = createChannel("SENDER1");
        channel1.setEnabled(true);
        Channel channel2 = createChannel("SENDER2");
        channel2.setEnabled(false);
        when(channelMapper.findAll()).thenReturn(Arrays.asList(channel1, channel2));
        manager.initSmsSenders();
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testInitSmsSenders_ChannelsExist() {
        Channel channel1 = createChannel("SENDER1");
        channel1.setEnabled(true);
        Channel channel2 = createChannel("SENDER2");
        channel2.setEnabled(false);
        when(channelMapper.findAll()).thenReturn(Arrays.asList(channel1, channel2));
        manager.initSmsSenders();
        assertFalse(manager.getSmsSenders().isEmpty());
        assertEquals(1, manager.getSmsSenders().size());

        manager.initSmsSenders();
        assertFalse(manager.getSmsSenders().isEmpty());
        assertEquals(1, manager.getSmsSenders().size());
        verify(channelMapper, times(1)).findAll();
    }

    @Test
    public void testSendMessages_NoSenders() {
        Whitebox.setInternalState(manager, "smsSenders", Collections.<SmsSender> emptyList());
        manager.sendMessages();
    }

    @Test
    public void testSendMessages_SendersExist() {
        SmsSender smsSender1 = mock(SmsSender.class);
        SmsSender smsSender2 = mock(SmsSender.class);
        Whitebox.setInternalState(manager, "smsSenders", Arrays.asList(smsSender1, smsSender2));
        manager.sendMessages();
        verify(smsSender1, times(1)).sendMessages();
        verify(smsSender2, times(1)).sendMessages();
    }

    @Test
    public void testCheckMessages_NoSenders() {
        Whitebox.setInternalState(manager, "smsSenders", Collections.<SmsSender> emptyList());
        manager.checkMessages();
    }

    @Test
    public void testCheckMessages_SendersExist() {
        SmsSender smsSender1 = mock(SmsSender.class);
        SmsSender smsSender2 = mock(SmsSender.class);
        Whitebox.setInternalState(manager, "smsSenders", Arrays.asList(smsSender1, smsSender2));
        manager.checkMessages();
        verify(smsSender1, times(1)).checkMessages();
        verify(smsSender2, times(1)).checkMessages();
    }

    @Test
    public void testSendPingPackage_NoSenders() {
        Whitebox.setInternalState(manager, "smsSenders", Collections.<SmsSender> emptyList());
        manager.sendPingPackage();
    }

    @Test
    public void testSendPingPackage_SendersExist() {
        SmsSender smsSender1 = mock(SmsSender.class);
        SmsSender smsSender2 = mock(SmsSender.class);
        Whitebox.setInternalState(manager, "smsSenders", Arrays.asList(smsSender1, smsSender2));
        manager.sendPingPackage();
        verify(smsSender1, times(1)).sendPingPackage();
        verify(smsSender2, times(1)).sendPingPackage();
    }

    @Test
    public void testCloseAllSenders_NoSenders() {
        Whitebox.setInternalState(manager, "smsSenders", Collections.<SmsSender> emptyList());
        manager.closeAllSenders();
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testCloseAllSenders_SendersExist() {
        SmsSender smsSender1 = mock(SmsSender.class);
        SmsSender smsSender2 = mock(SmsSender.class);
        Whitebox.setInternalState(manager, "smsSenders",
                new ArrayList<SmsSender>(Arrays.asList(smsSender1, smsSender2)));
        manager.closeAllSenders();
        assertTrue(manager.getSmsSenders().isEmpty());
        verify(smsSender1, times(1)).closeConnection();
        verify(smsSender2, times(1)).closeConnection();
    }

    @Test
    public void testCloseSender_Null() {
        Whitebox.setInternalState(manager, "smsSenders", Collections.<SmsSender> emptyList());
        manager.closeSender(null);
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testCloseSender_NoSenders() {
        Whitebox.setInternalState(manager, "smsSenders", Collections.<SmsSender> emptyList());
        manager.closeSender(createChannel(null));
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testCloseSender_SendersExist_SenderNotFound() {
        SmsSender smsSender1 = mock(SmsSender.class);
        when(smsSender1.getChannel()).thenReturn(createChannel("SENDER1"));
        SmsSender smsSender2 = mock(SmsSender.class);
        when(smsSender2.getChannel()).thenReturn(createChannel("SENDER2"));
        Whitebox.setInternalState(manager, "smsSenders",
                new ArrayList<SmsSender>(Arrays.asList(smsSender1, smsSender2)));
        manager.closeSender(createChannel("SENDER3"));
        assertFalse(manager.getSmsSenders().isEmpty());
        assertEquals(2, manager.getSmsSenders().size());
        verify(smsSender1, times(0)).closeConnection();
        verify(smsSender2, times(0)).closeConnection();
    }

    @Test
    public void testCloseSender_SendersExist_SenderFound() {
        SmsSender smsSender1 = mock(SmsSender.class);
        when(smsSender1.getChannel()).thenReturn(createChannel("SENDER1"));
        SmsSender smsSender2 = mock(SmsSender.class);
        when(smsSender2.getChannel()).thenReturn(createChannel("SENDER2"));
        Whitebox.setInternalState(manager, "smsSenders",
                new ArrayList<SmsSender>(Arrays.asList(smsSender1, smsSender2)));
        manager.closeSender(createChannel("SENDER2"));
        assertFalse(manager.getSmsSenders().isEmpty());
        assertEquals(1, manager.getSmsSenders().size());
        verify(smsSender1, times(0)).closeConnection();
        verify(smsSender2, times(1)).closeConnection();
    }

    @Test
    public void testAddSender_ChannelDisabled() {
        Whitebox.setInternalState(manager, "smsSenders", new ArrayList<SmsSender>());
        Channel channel = createChannel(null);
        channel.setEnabled(false);
        manager.addSender(channel);
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testAddSender_ChannelEnabled() {
        Whitebox.setInternalState(manager, "smsSenders", new ArrayList<SmsSender>());
        Channel channel = createChannel(null);
        channel.setEnabled(true);
        manager.addSender(channel);
        assertFalse(manager.getSmsSenders().isEmpty());
        assertEquals(1, manager.getSmsSenders().size());
    }

    @Test
    public void testStartSender_Null() {
        Whitebox.setInternalState(manager, "smsSenders", Collections.<SmsSender> emptyList());
        manager.startSender(null);
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testStartSender_NoSenders() {
        Whitebox.setInternalState(manager, "smsSenders", Collections.<SmsSender> emptyList());
        manager.startSender(createChannel("SENDER3"));
        assertTrue(manager.getSmsSenders().isEmpty());
    }

    @Test
    public void testStartSender_SendersExist_SenderNotFound() {
        SmsSender smsSender1 = mock(SmsSender.class);
        when(smsSender1.getChannel()).thenReturn(createChannel("SENDER1"));
        SmsSender smsSender2 = mock(SmsSender.class);
        when(smsSender2.getChannel()).thenReturn(createChannel("SENDER2"));
        Whitebox.setInternalState(manager, "smsSenders",
                new ArrayList<SmsSender>(Arrays.asList(smsSender1, smsSender2)));
        manager.startSender(createChannel("SENDER3"));
        assertFalse(manager.getSmsSenders().isEmpty());
        assertEquals(2, manager.getSmsSenders().size());
        verify(smsSender1, times(0)).connect();
        verify(smsSender2, times(0)).connect();
    }

    @Test
    public void testStartSender_SendersExist_SenderFound() {
        SmsSender smsSender1 = mock(SmsSender.class);
        when(smsSender1.getChannel()).thenReturn(createChannel("SENDER1"));
        SmsSender smsSender2 = mock(SmsSender.class);
        when(smsSender2.getChannel()).thenReturn(createChannel("SENDER2"));
        Whitebox.setInternalState(manager, "smsSenders",
                new ArrayList<SmsSender>(Arrays.asList(smsSender1, smsSender2)));
        manager.startSender(createChannel("SENDER2"));
        assertFalse(manager.getSmsSenders().isEmpty());
        assertEquals(2, manager.getSmsSenders().size());
        verify(smsSender1, times(0)).connect();
        verify(smsSender2, times(1)).connect();
    }

    private Channel createChannel(String name) {
        Channel channel = new Channel();
        channel.setName(name);
        return channel;
    }

}
