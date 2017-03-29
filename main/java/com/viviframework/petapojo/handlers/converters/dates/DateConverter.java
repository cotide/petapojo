package com.viviframework.petapojo.handlers.converters.dates;

import java.util.Date;

/**
 * java.util.Date 时间转换器
 */
public class DateConverter extends AbstractDateConverter<Date> {

    public static final DateConverter instance = new DateConverter();

    protected DateConverter() {
        super(Date.class);
    }

    @Override
    protected Date fromMilliseconds(long time) {
        return new Date(time);
    }
}
