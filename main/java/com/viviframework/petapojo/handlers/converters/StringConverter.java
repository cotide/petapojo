package com.viviframework.petapojo.handlers.converters;

import com.viviframework.petapojo.tools.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * String类型转换器
 */
public class StringConverter implements Converter<String> {
    @Override
    public String convert(Object val) throws ConvertException {
        if (val == null) {
            return null;
        }

        if (val instanceof Clob) {
            Clob clobVal = (Clob) val;
            try {
                try {
                    return clobVal.getSubString(1, (int) clobVal.length());
                } catch (SQLException ex) {
                    throw new ConvertException("error converting clob to String", ex);
                }
            } finally {
                try {
                    clobVal.free();
                } catch (Throwable ignore) {

                }
            }
        }

        if (val instanceof Reader) {
            Reader reader = (Reader) val;
            try {
                try {
                    return IOUtils.toString(reader);
                } catch (IOException ex) {
                    throw new ConvertException("erro converting reader to String", ex);
                }
            } finally {
                try {
                    reader.close();
                } catch (Throwable ignore) {

                }
            }
        }

        return val.toString().trim();
    }
}
