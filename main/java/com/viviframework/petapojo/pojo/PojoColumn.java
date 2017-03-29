package com.viviframework.petapojo.pojo;


import com.viviframework.petapojo.enums.IEnumMessage;
import com.viviframework.petapojo.handlers.converters.Convert;
import com.viviframework.petapojo.handlers.converters.ConvertException;
import com.viviframework.petapojo.handlers.converters.Converter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Field;

public class PojoColumn {

    private String columnName;

    private Field field;

    public void setValue(Object target, Object val) throws ConvertException {
        BeanWrapper bw = new BeanWrapperImpl(target);
        Converter converter = Convert.getConverterIfExists(field.getType());
        bw.setPropertyValue(field.getName(), converter.convert(val));
    }

    public Object getValue(Object target) {
        BeanWrapper bw = new BeanWrapperImpl(target);
        Object object = bw.getPropertyValue(field.getName());

        if(object == null)
            return null;

        boolean isEnum = object.getClass().isEnum();
        if (object.getClass().isEnum() && IEnumMessage.class.isAssignableFrom(object.getClass())) {
            return ((IEnumMessage) object).getValue();
        }

        if (object instanceof DateTime) {
            DateTime dateTime = (DateTime) object;
            return dateTime.toString("yyyy-MM-dd HH:mm:ss");
        }

        return object;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
