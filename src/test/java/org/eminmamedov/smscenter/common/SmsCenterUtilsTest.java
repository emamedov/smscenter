package org.eminmamedov.smscenter.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SmsCenterUtilsTest extends SpringTestSupport {

    @Autowired
    private SmsCenterUtils smsCenterUtils;

    @Test
    public void testCheckNumberNull() {
        String number = smsCenterUtils.checkNumber(null);
        assertNull(number);
    }

    @Test
    public void testCheckNumberEmptyString() {
        String number = smsCenterUtils.checkNumber("");
        assertNull(number);
    }

    @Test
    public void testCheckNumberWrongNumber1() {
        String number = smsCenterUtils.checkNumber("A345678");
        assertNull(number);
    }

    @Test
    public void testCheckNumberWrongNumber2() {
        String number = smsCenterUtils.checkNumber("12345678910");
        assertNull(number);
    }

    @Test
    public void testCheckNumberWrongOk1() {
        String number = smsCenterUtils.checkNumber("9853869839");
        assertNotNull(number);
        assertEquals("79853869839", number);
    }

    @Test
    public void testCheckNumberWrongOk2() {
        String number = smsCenterUtils.checkNumber("8-985-386 98 39");
        assertNotNull(number);
        assertEquals("79853869839", number);
    }

    @Test
    public void testCheckNumberWrongOk3() {
        String number = smsCenterUtils.checkNumber("+7(985)386 98 39");
        assertNotNull(number);
        assertEquals("79853869839", number);
    }

    @Test
    public void testSplitMessageNull() {
        List<String> splitMessage = smsCenterUtils.splitMessage(null);
        assertNotNull(splitMessage);
        assertTrue(splitMessage.isEmpty());
    }

    @Test
    public void testSplitMessageEmpty() {
        List<String> splitMessage = smsCenterUtils.splitMessage("");
        assertNotNull(splitMessage);
        assertTrue(splitMessage.isEmpty());
    }

    @Test
    public void testSplitMessageLatinOneMessage() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 160; i++) {
            sb.append("a");
        }
        List<String> splitMessage = smsCenterUtils.splitMessage(sb.toString());
        assertNotNull(splitMessage);
        assertFalse(splitMessage.isEmpty());
        assertEquals(1, splitMessage.size());
        assertEquals(sb.toString(), splitMessage.get(0));
    }

    @Test
    public void testSplitMessageLatinTwoMessages() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 161; i++) {
            sb.append("a");
        }
        List<String> splitMessage = smsCenterUtils.splitMessage(sb.toString());
        assertNotNull(splitMessage);
        assertFalse(splitMessage.isEmpty());
        assertEquals(2, splitMessage.size());
        assertEquals(StringUtils.substring(sb.toString(), 0, 153), splitMessage.get(0));
        assertEquals(StringUtils.substring(sb.toString(), 153), splitMessage.get(1));
    }

    @Test
    public void testSplitMessageNonLatinTwoMessages() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 160; i++) {
            sb.append("a");
        }
        sb.append("â");
        List<String> splitMessage = smsCenterUtils.splitMessage(sb.toString());
        assertNotNull(splitMessage);
        assertFalse(splitMessage.isEmpty());
        assertEquals(3, splitMessage.size());
        assertEquals(StringUtils.substring(sb.toString(), 0, 67), splitMessage.get(0));
        assertEquals(StringUtils.substring(sb.toString(), 67, 134), splitMessage.get(1));
        assertEquals(StringUtils.substring(sb.toString(), 134), splitMessage.get(2));
    }

}
