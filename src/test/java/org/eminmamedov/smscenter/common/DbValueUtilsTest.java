package org.eminmamedov.smscenter.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class DbValueUtilsTest {

    @Test
    public void testGetDbValue_Null() {
        assertNull(DbValueUtils.getDbValue(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDbValue_WithoutDbValue() {
        DbValueUtils.getDbValue(WithoutDbValueEnum.VALUE);
    }

    @Test
    public void testGetDbValue_StringValue() {
        assertEquals("value", DbValueUtils.getDbValue(StringEnum.VALUE));
    }

    @Test
    public void testGetDbValue_IntValue() {
        assertEquals(1, DbValueUtils.getDbValue(IntegerEnum.VALUE));
    }

    @Test
    public void testGetEnumValueByDbValue_Null() {
        assertNull(DbValueUtils.getEnumValueByDbValue(StringEnum.class, null));
    }

    @Test
    public void testGetEnumValueByDbValue_String_NotFound() {
        assertNull(DbValueUtils.getEnumValueByDbValue(StringEnum.class, "value1"));
    }

    @Test
    public void testGetEnumValueByDbValue_Int_NotFound() {
        assertNull(DbValueUtils.getEnumValueByDbValue(IntegerEnum.class, 2));
    }

    @Test
    public void testGetEnumValueByDbValue_String_Found() {
        assertEquals(StringEnum.VALUE, DbValueUtils.getEnumValueByDbValue(StringEnum.class, "value"));
    }

    @Test
    public void testGetEnumValueByDbValue_Int_Found() {
        assertEquals(IntegerEnum.VALUE, DbValueUtils.getEnumValueByDbValue(IntegerEnum.class, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnumValueByDbValue_EnumWithoutDbValue() {
        DbValueUtils.getEnumValueByDbValue(WithoutDbValueEnum.class, "value");
    }

    private enum StringEnum {
        @DBValue(stringValue = "value")
        VALUE
    }

    private enum IntegerEnum {
        @DBValue(intValue = 1)
        VALUE
    }

    private enum WithoutDbValueEnum {
        VALUE
    }

}
