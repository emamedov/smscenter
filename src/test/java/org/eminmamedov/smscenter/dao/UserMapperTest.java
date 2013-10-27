package org.eminmamedov.smscenter.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.apache.commons.codec.digest.DigestUtils;
import org.eminmamedov.smscenter.common.SpringTestSupport;
import org.eminmamedov.smscenter.datamodel.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

@TransactionConfiguration(defaultRollback = true)
public class UserMapperTest extends SpringTestSupport {

    @Autowired
    private UserMapper userMapper;
    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setName("testUser");
        testUser.setPassword("testPass");
        testUser.setAllowedIp(null);
        testUser.setEnabled(true);
        userMapper.insert(testUser);
    }

    @After
    public void tearDown() {
        userMapper.delete(testUser);
    }

    @Test
    public void testGetUserOk() {
        User foundUser = userMapper.getUser("testUser", DigestUtils.sha512Hex("testPass"));
        assertEqualsUsers(testUser, foundUser);
    }

    @Test
    public void testGetUserWrongLogin() {
        User foundUser = userMapper.getUser("test1", "testPass");
        assertNull(foundUser);
    }

    @Test
    public void testGetUserWrongPassword() {
        User foundUser = userMapper.getUser("testUser", "testPass1");
        assertNull(foundUser);
    }

    @Test
    public void testUpdateOk() {
        testUser.setLastLogin(new GregorianCalendar(2011, 4, 5).getTime());
        userMapper.update(testUser);
        User foundUser = userMapper.getUser(testUser.getName(), testUser.getEncryptedPassword());
        assertEqualsUsers(testUser, foundUser);
    }

    @Test
    public void testWrongPassword() {
        // wrong password ten times should lock user
        for (int i = 0; i <= 11; i++) {
            User foundUser = userMapper.getUser(testUser.getName(), testUser.getEncryptedPassword());
            assertNotNull(foundUser);
            assertTrue(foundUser.isEnabled());
            userMapper.wrongPassword(testUser.getName());
        }
        User foundUser = userMapper.getUser(testUser.getName(), testUser.getEncryptedPassword());
        assertNotNull(foundUser);
        assertFalse(foundUser.isEnabled());
    }

    private void assertEqualsUsers(User expected, User actual) {
        assertNotNull(actual);
        assertEquals(expected.getAllowedIp(), actual.getAllowedIp());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.isEnabled(), actual.isEnabled());
    }

}
