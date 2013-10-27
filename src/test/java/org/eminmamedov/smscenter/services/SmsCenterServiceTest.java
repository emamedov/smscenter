package org.eminmamedov.smscenter.services;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.eminmamedov.smscenter.dao.SmsMapper;
import org.eminmamedov.smscenter.dao.UserMapper;
import org.eminmamedov.smscenter.datamodel.SMSMessage;
import org.eminmamedov.smscenter.datamodel.Sender;
import org.eminmamedov.smscenter.datamodel.User;
import org.eminmamedov.smscenter.exceptions.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmsCenterServiceTest {

    private SmsCenterService service;
    @Mock
    private SmsMapper smsMapper;
    @Mock
    private UserMapper userMapper;

    @Before
    public void setUp() {
        service = new SmsCenterService();
        when(smsMapper.getLastSmsId()).thenReturn(15);
        service.setSmsMapper(smsMapper);
        service.setUserMapper(userMapper);
        service.setAllSendersSign("ALL_SENDERS");
        service.afterPropertiesSet();
    }

    @Test
    public void testReserveServerId() {
        Long id = service.reserveServerId();
        assertNotNull(id);
        assertEquals(new Long(16), id);
        id = service.reserveServerId();
        assertNotNull(id);
        assertEquals(new Long(17), id);
    }

    @Test
    public void testAddMessagesNull() {
        service.addMessages(null);
        verify(smsMapper, times(0)).addMessages(anyListOf(SMSMessage.class));
    }

    @Test
    public void testAddMessagesOk() {
        List<SMSMessage> messages = new ArrayList<SMSMessage>();
        messages.add(new SMSMessage());
        service.addMessages(messages);
        verify(smsMapper).addMessages(same(messages));
    }

    @Test
    public void testGetMessagesNull() {
        List<SMSMessage> foundMessages = service.getMessages(null);
        verify(smsMapper, times(0)).getMessages(anyListOf(Long.class));
        assertNotNull(foundMessages);
        assertTrue(foundMessages.isEmpty());
    }

    @Test
    public void testGetMessagesOk() {
        List<Long> ids = new ArrayList<Long>();
        ids.add(new Long(0));
        List<SMSMessage> messages = new ArrayList<SMSMessage>();
        messages.add(new SMSMessage());
        when(smsMapper.getMessages(ids)).thenReturn(messages);
        List<SMSMessage> foundMessages = service.getMessages(ids);
        verify(smsMapper).getMessages(same(ids));
        assertNotNull(foundMessages);
        assertSame(messages, foundMessages);
        assertFalse(foundMessages.isEmpty());
        assertEquals(1, foundMessages.size());
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserNullLogin() {
        service.getUser(null, null, null);
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserNullPassword() {
        service.getUser("test", null, null);
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserNoIpRestrictionUserNotFound() {
        String login = "testLogin";
        String password = "test";
        when(userMapper.getUser(login, password)).thenReturn(null);
        service.getUser(login, password, null);
    }

    @Test
    public void testGetUserNoIpRestrictionUserFound() {
        String login = "testLogin";
        String password = "test";
        User user = new User();
        user.setAllowedIp(null);
        user.setName("testLogin");
        when(userMapper.getUser(login, DigestUtils.sha512Hex(password))).thenReturn(user);
        User foundUser = service.getUser(login, password, null);
        assertNotNull(foundUser);
        assertSame(user, foundUser);
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserIpRestrictionUserNotFound() {
        String login = "testLogin";
        String password = "test";
        User user = new User();
        user.setAllowedIp("123");
        user.setName("testLogin");
        when(userMapper.getUser(login, password)).thenReturn(user);
        service.getUser(login, password, "456");
    }

    @Test
    public void testGetUserIpRestrictionUserFound() {
        String login = "testLogin";
        String password = "test";
        User user = new User();
        user.setAllowedIp("123");
        user.setName("testLogin");
        user.setEnabled(true);
        when(userMapper.getUser(login, DigestUtils.sha512Hex(password))).thenReturn(user);
        User foundUser = service.getUser(login, password, "123");
        assertNotNull(foundUser);
        assertSame(user, foundUser);
        verify(userMapper).update(same(foundUser));
    }

    @Test(expected = NullPointerException.class)
    public void testGetSender_Nulls() {
        service.getSender(null, null);
    }

    @Test
    public void testGetSender_EmptyList_SenderSignNull() {
        User user = new User();
        user.setSenders(Collections.<Sender> emptyList());
        Sender sender = service.getSender(user, null);
        assertNull(sender);
    }

    @Test
    public void testGetSender_EmptyList_SenderSignNotNull() {
        User user = new User();
        user.setSenders(Collections.<Sender> emptyList());
        Sender sender = service.getSender(user, null);
        assertNull(sender);
    }

    @Test
    public void testGetSender_NonEmptyList_SenderSignNull() {
        User user = new User();
        Sender sender = createSender("test");
        user.setSenders(Arrays.asList(createSender("ALL_SENDERS"), sender));
        Sender foundSender = service.getSender(user, null);
        assertNotNull(foundSender);
        assertSame(sender, foundSender);
    }

    @Test
    public void testGetSender_NonEmptyList_SenderSignNotNull_NotFound() {
        User user = new User();
        Sender sender = createSender("ALL_SENDERS");
        user.setSenders(Arrays.asList(sender, createSender("test")));
        Sender foundSender = service.getSender(user, "test1");
        assertNotNull(foundSender);
        assertSame(sender, foundSender);
    }

    @Test
    public void testGetSender_NonEmptyList_SenderSignNotNull_NotFound_NoAllSenders() {
        User user = new User();
        user.setSenders(Arrays.asList(createSender("test1"), createSender("test2")));
        Sender foundSender = service.getSender(user, "test");
        assertNull(foundSender);
    }

    @Test
    public void testGetSender_NonEmptyList_SenderSignNotNull_Found() {
        User user = new User();
        Sender sender = createSender("test");
        user.setSenders(Arrays.asList(sender, createSender("ALL_SENDERS")));
        Sender foundSender = service.getSender(user, "test");
        assertNotNull(foundSender);
        assertSame(sender, foundSender);
    }

    @Test
    public void testGetUpdatedMessages() {
        User user = new User();
        service.getUpdatedMessages(user, 100);
        verify(smsMapper, times(1)).getUpdatedMessages(same(user), eq(100));
    }

    @Test
    public void testSetInformedFlagForMessage() {
        service.setInformedFlagForMessage(1L, true);
        verify(smsMapper, times(1)).setInformed(eq(1L), eq(true));
    }

    private Sender createSender(String sign) {
        Sender sender = new Sender();
        sender.setSign(sign);
        return sender;
    }

}
