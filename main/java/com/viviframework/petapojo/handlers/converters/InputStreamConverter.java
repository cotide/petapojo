package com.viviframework.petapojo.handlers.converters;

import java.io.ByteArrayInputStream;

/**
 * InputStream转换器
 */
public class InputStreamConverter implements Converter<ByteArrayInputStream> {
    @Override
    public ByteArrayInputStream convert(Object val) throws ConvertException {
        if (val == null) {
            return null;
        }

        try {
            return new ByteArrayInputStream(new ByteArrayConverter().convert(val));
        } catch (ConvertException ex) {
            throw new ConvertException("cannot convert type " + val.getClass().toString() + " to InputStream");
        }
    }
}
