package org.eminmamedov.smscenter.dao;

import static org.junit.Assert.*;

import org.eminmamedov.smscenter.common.SpringTestSupport;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

@TransactionConfiguration(defaultRollback = true)
public class SenderMapperTest extends SpringTestSupport {

    @Autowired
    private SenderMapper senderMapper;
    private Sender testSender;
    
    @Before
    public void setUp() {
        testSender = new Sender();
        testSender.setSign("TEST_SENDER");
        senderMapper.insert(testSender);        
    }
    
    @After
    public void tearDown() {
        senderMapper.delete(testSender);
    }
    
    @Test
    public void testGetSenderOk() {
        Sender foundSender = senderMapper.getSender("TEST_SENDER");
        assertNotNull(foundSender);
        assertEquals(testSender.getSign(), foundSender.getSign());
    }

    @Test
    public void testGetSenderNotFound() {
        Sender foundSender = senderMapper.getSender("NOT_FOUND");
        assertNull(foundSender);
    }

}
