package com.viviframework.petapojo.handlers.converters.dates.joda;

import com.viviframework.petapojo.handlers.converters.ConvertException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import com.viviframework.petapojo.handlers.converters.Converter;

/**
 * 时间类型转换器
 */
public class DateTimeConverter implements Converter<DateTime> {

    private final DateTimeZone timeZone;

    public DateTimeConverter(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public DateTimeConverter() {
        this(DateTimeZone.getDefault());
    }

    public DateTime convert(Object val) throws ConvertException {

        if (val == null)
            return null;

        try {
            return new LocalDateTime(val).toDateTime(timeZone);
        } catch (IllegalArgumentException ex) {
            throw new ConvertException("cannot convert type " + val.getClass().toString() + " to jodatetime", ex);
        }
    }
}
