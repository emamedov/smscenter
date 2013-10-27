package org.eminmamedov.smscenter.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

public class EnumAnnotatedTypeHandlerTest {

    @Test
    public void testSetNonNullParameter_DbValueString() throws SQLException {
        EnumAnnotatedTypeHandler<StringEnum> handler = new EnumAnnotatedTypeHandler<StringEnum>(StringEnum.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        handler.setNonNullParameter(ps, 3, StringEnum.VALUE, null);
        verify(ps, times(1)).setString(eq(3), eq("value"));
    }

    @Test
    public void testSetNonNullParameter_DbValueInt() throws SQLException {
        EnumAnnotatedTypeHandler<IntegerEnum> handler = new EnumAnnotatedTypeHandler<IntegerEnum>(IntegerEnum.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        handler.setNonNullParameter(ps, 3, IntegerEnum.VALUE, null);
        verify(ps, times(1)).setInt(eq(3), eq(1));
    }

    @Test
    public void testGetNullableResult_ResultSet_ColumnName_DBValueString_Ok() throws SQLException {
        EnumAnnotatedTypeHandler<StringEnum> handler = new EnumAnnotatedTypeHandler<StringEnum>(StringEnum.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(eq("column"))).thenReturn("value");
        StringEnum enumValue = handler.getNullableResult(rs, "column");
        assertEquals(StringEnum.VALUE, enumValue);
    }

    @Test
    public void testGetNullableResult_ResultSet_ColumnName_DBValueInt_Ok() throws SQLException {
        EnumAnnotatedTypeHandler<IntegerEnum> handler = new EnumAnnotatedTypeHandler<IntegerEnum>(IntegerEnum.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(eq("column"))).thenReturn("1");
        IntegerEnum enumValue = handler.getNullableResult(rs, "column");
        assertEquals(IntegerEnum.VALUE, enumValue);
    }

    @Test
    public void testGetNullableResult_ResultSet_ColumnName_DBValueString_Null() throws SQLException {
        EnumAnnotatedTypeHandler<StringEnum> handler = new EnumAnnotatedTypeHandler<StringEnum>(StringEnum.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(eq("column"))).thenReturn(null);
        StringEnum enumValue = handler.getNullableResult(rs, "column");
        assertNull(enumValue);
    }

    @Test
    public void testGetNullableResult_ResultSet_ColumnIndex_DBValueString_Ok() throws SQLException {
        EnumAnnotatedTypeHandler<StringEnum> handler = new EnumAnnotatedTypeHandler<StringEnum>(StringEnum.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(eq(3))).thenReturn("value");
        StringEnum enumValue = handler.getNullableResult(rs, 3);
        assertEquals(StringEnum.VALUE, enumValue);
    }

    @Test
    public void testGetNullableResult_ResultSet_ColumnIndex_DBValueInt_Ok() throws SQLException {
        EnumAnnotatedTypeHandler<IntegerEnum> handler = new EnumAnnotatedTypeHandler<IntegerEnum>(IntegerEnum.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(eq(3))).thenReturn("1");
        IntegerEnum enumValue = handler.getNullableResult(rs, 3);
        assertEquals(IntegerEnum.VALUE, enumValue);
    }

    @Test
    public void testGetNullableResult_ResultSet_ColumnIndex_DBValueString_Null() throws SQLException {
        EnumAnnotatedTypeHandler<StringEnum> handler = new EnumAnnotatedTypeHandler<StringEnum>(StringEnum.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(eq(3))).thenReturn(null);
        StringEnum enumValue = handler.getNullableResult(rs, 3);
        assertNull(enumValue);
    }

    @Test
    public void testGetNullableResult_CallableStatement_ColumnIndex_DBValueString_Ok() throws SQLException {
        EnumAnnotatedTypeHandler<StringEnum> handler = new EnumAnnotatedTypeHandler<StringEnum>(StringEnum.class);
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(eq(3))).thenReturn("value");
        StringEnum enumValue = handler.getNullableResult(cs, 3);
        assertEquals(StringEnum.VALUE, enumValue);
    }

    @Test
    public void testGetNullableResult_CallableStatement_ColumnIndex_DBValueInt_Ok() throws SQLException {
        EnumAnnotatedTypeHandler<IntegerEnum> handler = new EnumAnnotatedTypeHandler<IntegerEnum>(IntegerEnum.class);
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(eq(3))).thenReturn("1");
        IntegerEnum enumValue = handler.getNullableResult(cs, 3);
        assertEquals(IntegerEnum.VALUE, enumValue);
    }

    @Test
    public void testGetNullableResult_CallableStatement_ColumnIndex_DBValueString_Null() throws SQLException {
        EnumAnnotatedTypeHandler<StringEnum> handler = new EnumAnnotatedTypeHandler<StringEnum>(StringEnum.class);
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(eq(3))).thenReturn(null);
        StringEnum enumValue = handler.getNullableResult(cs, 3);
        assertNull(enumValue);
    }

    private enum StringEnum {
        @DBValue(stringValue = "value")
        VALUE
    }

    private enum IntegerEnum {
        @DBValue(intValue = 1)
        VALUE
    }

}
