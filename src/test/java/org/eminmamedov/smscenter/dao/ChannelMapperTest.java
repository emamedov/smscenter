package org.eminmamedov.smscenter.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eminmamedov.smscenter.common.SpringTestSupport;
import org.eminmamedov.smscenter.datamodel.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

@TransactionConfiguration(defaultRollback = true)
public class ChannelMapperTest extends SpringTestSupport {

    @Autowired
    private ChannelMapper channelMapper;
    private Channel channel1;
    private Channel channel2;

    @Before
    public void setUp() {
        channel1 = new Channel();
        channel1.setName("Beeline");
        channel1.setLogin("testBee");
        channel1.setPassword("testBeePass");
        channel1.setPort(80);
        channel1.setEnabled(true);
        channel1.setHost("123");
        channel1.setSendSpeed(100);
        channel1.setCheckSpeed(110);
        channel1.setBindType("1");

        channel2 = new Channel();
        channel2.setName("MTS");
        channel2.setLogin("testMts");
        channel2.setPassword("testMtsPass");
        channel2.setPort(90);
        channel2.setEnabled(false);
        channel2.setHost("456");
        channel2.setSendSpeed(110);
        channel2.setCheckSpeed(120);
        channel2.setBindNPI(1);
        channel2.setBindTON(2);
        channel2.setBindType("1");
        channel2.setDestAddrNPI(3);
        channel2.setDestAddrTON(4);
        channel2.setSourceAddrNPI(5);
        channel2.setSourceAddrTON(6);

        channelMapper.insert(channel1);
        channelMapper.insert(channel2);
    }

    @After
    public void tearDown() {
        channelMapper.delete(channel1);
        channelMapper.delete(channel2);
    }

    @Test
    public void testFindAll() {
        List<Channel> channels = channelMapper.findAll();
        assertNotNull(channels);
        assertFalse(channels.isEmpty());

        Channel beeChannel = findChannel(channels, "Beeline");
        assertChannel(channel1, beeChannel);
        Channel mtsChannel = findChannel(channels, "MTS");
        assertChannel(channel2, mtsChannel);
    }

    private void assertChannel(Channel expected, Channel actual) {
        assertNotNull(actual);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getLogin(), actual.getLogin());
        assertEquals(expected.getPassword(), actual.getPassword());
        assertEquals(expected.getPort(), actual.getPort());
        assertEquals(expected.isEnabled(), actual.isEnabled());
        assertEquals(expected.getHost(), actual.getHost());
        assertEquals(expected.getSendSpeed(), actual.getSendSpeed());
        assertEquals(expected.getCheckSpeed(), actual.getCheckSpeed());
        assertEquals(expected.getBindNPI(), actual.getBindNPI());
        assertEquals(expected.getBindTON(), actual.getBindTON());
        assertEquals(expected.getBindType(), actual.getBindType());
        assertEquals(expected.getDestAddrNPI(), actual.getDestAddrNPI());
        assertEquals(expected.getDestAddrTON(), actual.getDestAddrTON());
        assertEquals(expected.getSourceAddrNPI(), actual.getSourceAddrNPI());
        assertEquals(expected.getSourceAddrTON(), actual.getSourceAddrTON());
    }

    private Channel findChannel(List<Channel> channels, String name) {
        for (Channel channel : channels) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        return null;
    }

}
