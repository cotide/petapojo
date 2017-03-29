package com.viviframework.petapojo.handlers.converters;

import com.viviframework.petapojo.handlers.converters.dates.joda.DateTimeConverter;
import com.viviframework.petapojo.handlers.converters.number.*;
import com.viviframework.petapojo.tools.FeatureDetector;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import com.viviframework.petapojo.handlers.converters.dates.AbstractDateConverter;
import com.viviframework.petapojo.handlers.converters.dates.DateConverter;
import com.viviframework.petapojo.handlers.converters.dates.joda.LocalTimeConverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 类型转换器工厂
 */
public class Convert {
    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl = rrwl.readLock();
    private static final ReentrantReadWriteLock.WriteLock wl = rrwl.writeLock();
    private static volatile EnumConverterFactory registeredEnumConverterFactory = new DefaultEnumConverterFactory();
    private static Map<Class<?>, Converter<?>> registeredConverters = new HashMap<Class<?>, Converter<?>>();

    static {
        fillDefaults(registeredConverters);
    }

    public static <T> Converter<T> throwIfNull(Class<T> clazz, Converter<T> converter) throws ConvertException {
        if (converter == null)
            throw new ConvertException("Cannot find converter:" + clazz.getName());

        return converter;
    }

    public static <T> Converter<T> getConverterIfExists(Class<T> clazz) {
        Converter c;
        rl.lock();
        try {
            c = registeredConverters.get(clazz);
        } finally {
            rl.unlock();
        }

        if (c != null) {
            return c;
        }

        if (clazz.isEnum()) {
            return registeredEnumConverterFactory.newConverter((Class) clazz);
        }

        return null;
    }

    private static void fillDefaults(Map<Class<?>, Converter<?>> mapToFill) {
        mapToFill.put(Integer.class, new IntegerConverter(false));
        mapToFill.put(int.class, new IntegerConverter(true));

        mapToFill.put(Byte.class, new ByteConverter(false));
        mapToFill.put(byte.class, new ByteConverter(true));

        mapToFill.put(Short.class, new ShortConverter(false));
        mapToFill.put(short.class, new ShortConverter(true));

        mapToFill.put(Long.class, new LongConverter(false));
        mapToFill.put(long.class, new LongConverter(true));

        mapToFill.put(Float.class, new FloatConverter(false));
        mapToFill.put(float.class, new FloatConverter(true));

        mapToFill.put(Double.class, new DoubleConverter(false));
        mapToFill.put(double.class, new DoubleConverter(true));

        mapToFill.put(Boolean.class, new BooleanConverter());
        mapToFill.put(boolean.class, new BooleanConverter());

        mapToFill.put(BigDecimal.class, new BigDecimalConverter());

        mapToFill.put(String.class, new StringConverter());

        mapToFill.put(UUID.class, new UUIDConverter());

        mapToFill.put(byte[].class, new ByteArrayConverter());

        InputStreamConverter inputStreamConverter = new InputStreamConverter();
        mapToFill.put(InputStream.class, inputStreamConverter);
        mapToFill.put(ByteArrayInputStream.class, inputStreamConverter);

        mapToFill.put(java.util.Date.class, DateConverter.instance);
        mapToFill.put(Date.class, new AbstractDateConverter<Date>(Date.class) {
            @Override
            protected Date fromMilliseconds(long time) {
                return new Date(time);
            }
        });
        mapToFill.put(java.sql.Time.class, new AbstractDateConverter<java.sql.Time>(java.sql.Time.class) {
            @Override
            protected java.sql.Time fromMilliseconds(long time) {
                return new java.sql.Time(time);
            }
        });
        mapToFill.put(java.sql.Timestamp.class, new AbstractDateConverter<java.sql.Timestamp>(java.sql.Timestamp.class) {
            @Override
            protected java.sql.Timestamp fromMilliseconds(long time) {
                return new java.sql.Timestamp(time);
            }
        });
        if (FeatureDetector.isJodaTimeAvailable()) {
            mapToFill.put(DateTime.class, new DateTimeConverter());
            mapToFill.put(LocalTime.class, new LocalTimeConverter());
        }

    }


}
