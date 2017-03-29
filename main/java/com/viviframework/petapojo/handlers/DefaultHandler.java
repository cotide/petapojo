package com.viviframework.petapojo.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultHandler<T> implements ResultSetHandler<T> {

    private BasicProcessor basicProcessor = new BasicProcessor();
    private final Class<T> type;

    public DefaultHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public T handle(ResultSet rs) throws SQLException {
        return basicProcessor.createObject(rs, type);
    }
}
