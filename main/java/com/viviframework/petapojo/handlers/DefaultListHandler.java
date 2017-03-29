package com.viviframework.petapojo.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefaultListHandler<T> implements ResultSetHandler<List<T>> {
    private final Class<T> type;

    private final BasicProcessor basicProcessor = new BasicProcessor();

    public DefaultListHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public List<T> handle(ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<T>();

        if (!rs.next())
            return results;

        do {
            results.add(basicProcessor.createObject(rs, type));
        } while (rs.next());

        return results;
    }
}
