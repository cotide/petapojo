package com.viviframework.petapojo.handlers.converters;


import com.viviframework.petapojo.tools.IOUtils;

import java.io.InputStream;
import java.sql.Blob;

/**
 * byte[] 类型转换器
 */
public class ByteArrayConverter implements Converter<byte[]> {

    @Override
    public byte[] convert(Object val) throws ConvertException {
        if (val == null) {
            return null;
        }

        if (val instanceof Blob) {
            Blob b = (Blob) val;
            InputStream stream = null;

            try {
                try {
                    stream = b.getBinaryStream();
                    return IOUtils.toByteArray(stream);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Throwable ignore) {

                        }
                    }
                    try {
                        b.free();
                    } catch (Throwable ignore) {

                    }
                }
            } catch (Exception ex) {
                throw new ConvertException("cannot convert type " + val.getClass().toString() + " to byte[]", ex);
            }
        }

        if (val instanceof byte[]) {
            return (byte[]) val;
        }

        throw new ConvertException("cannot convert type " + val.getClass().toString() + " to byte[]");
    }
}
