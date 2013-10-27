package org.eminmamedov.smscenter.common;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;

/**
 * TypeHandler implementation that is used to store enum values in database.
 * Uses {@link DBValue} annotation to specify concrete database values for each
 * enum value.
 * 
 * @author Emin Mamedov
 * 
 */
public class EnumAnnotatedTypeHandler<E extends Enum<E>> extends TypedTypeHandler<E> {

    public EnumAnnotatedTypeHandler(Class<E> type) {
        super(type);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        Object dbValue = DbValueUtils.getDbValue(parameter);
        if (dbValue instanceof String) {
            ps.setString(i, (String) dbValue);
        } else if (dbValue instanceof Integer) {
            ps.setInt(i, (Integer) dbValue);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return convertStringToEnum(s);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return convertStringToEnum(s);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return convertStringToEnum(s);
    }

    private E convertStringToEnum(String s) {
        return s == null ? null : DbValueUtils.getEnumValueByDbValue(getJavaType(), s);
    }

}
