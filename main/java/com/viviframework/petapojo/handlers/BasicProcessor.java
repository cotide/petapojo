package com.viviframework.petapojo.handlers;

import com.viviframework.petapojo.handlers.converters.Convert;
import com.viviframework.petapojo.handlers.converters.ConvertException;
import com.viviframework.petapojo.handlers.converters.Converter;
import com.viviframework.petapojo.pojo.PojoColumn;
import com.viviframework.petapojo.pojo.PojoData;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Bean处理器
 */
public class BasicProcessor {

    public <T> T createObject(ResultSet rs, Class<T> type) throws SQLException {
        Converter<T> converter = Convert.getConverterIfExists(type);
        if (converter != null) {
            try {
                return converter.convert(rs.getObject(1));
            } catch (ConvertException e) {
                throw new SQLException("convert type error", e);
            }
        }

        Object bean = this.newInstance(type);

        PojoData pd = PojoData.forType(type);
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        try {
            for (int col = 1; col <= cols; col++) {
                String columName = rsmd.getColumnLabel(col);
                if (columName == null || columName.length() == 0) {
                    columName = rsmd.getColumnName(col);
                }

                if (!pd.getColumns().containsKey(columName.toLowerCase()))
                    continue;

                PojoColumn pc = pd.getColumns().get(columName.toLowerCase());

                Object rsValue;
                if (rsmd.getColumnTypeName(col).equalsIgnoreCase("TINYINT")) {
                    rsValue = rs.getInt(col);
                } else {
                    rsValue = rs.getObject(col);
                }

                pc.setValue(bean, rsValue);
            }
        } catch (Exception ex) {
            throw new SQLException("setPropertyValue error", ex);
        }

        return (T) bean;
    }

    private <T> T newInstance(Class<T> type) throws SQLException {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new SQLException("cannot create " + type.getName(), ex);
        }
    }
}
