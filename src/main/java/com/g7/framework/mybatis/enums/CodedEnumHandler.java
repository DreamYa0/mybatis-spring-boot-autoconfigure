package com.g7.framework.mybatis.enums;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dreamyao
 * @title mybatis enum 处理器
 * @date 2018/9/7 上午9:43
 * @since 1.0.0
 */
public class CodedEnumHandler<E> extends BaseTypeHandler<E> {

    private final Class<E> type;
    private final Method methodGetCode;
    private Method methodValueOf;

    public CodedEnumHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        if (!type.isEnum()) {
            throw new IllegalArgumentException("Type argument should be an enum");
        }
        this.type = type;
        try {
            methodValueOf = type.getMethod("valueOf", Integer.class);
            methodGetCode = type.getMethod("getCode");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Type argument should have static method: valueOf(Integer)");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) {

        try {
            Integer value = (Integer) methodGetCode.invoke(parameter);
            ps.setInt(i, value);
        } catch (Exception e) {
            throw new RuntimeException("Enumeration must have the getCode method", e);
        }

    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int i = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        } else {
            return convertIntegerToEnum(i);
        }
    }

    @SuppressWarnings("unchecked")
    private E convertIntegerToEnum(int i) {
        try {
            return (E) this.methodValueOf.invoke(null, i);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot convert " + i + " to " + type.getSimpleName() + ".", ex);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int i = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return convertIntegerToEnum(i);
        }
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int i = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        } else {
            return convertIntegerToEnum(i);
        }
    }
}
