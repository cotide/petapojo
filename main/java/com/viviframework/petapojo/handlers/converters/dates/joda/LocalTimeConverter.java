package com.viviframework.petapojo.handlers.converters.dates.joda;

import com.viviframework.petapojo.handlers.converters.ConvertException;
import com.viviframework.petapojo.handlers.converters.Converter;
import org.joda.time.LocalTime;

/**
 * 本地时间类型转换器
 */
public class LocalTimeConverter implements Converter<LocalTime> {

    public LocalTime convert(Object val) throws ConvertException {
        if (val == null)
            return null;

        try {
            return new LocalTime(val);
        } catch (IllegalArgumentException ex) {
            throw new ConvertException("Cannot convert type " + val.getClass().toString() + " to joda.LocalTime", ex);
        }
    }
}
