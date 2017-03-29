package com.viviframework.petapojo.pojo;

import com.viviframework.petapojo.annotations.Column;
import com.viviframework.petapojo.annotations.Ignore;
import com.viviframework.petapojo.annotations.PrimaryKey;
import com.viviframework.petapojo.annotations.TableName;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PojoData {

    private Class<?> clazz;

    private TableInfo tableInfo;

    private Map<String, PojoColumn> columns;


    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl = rrwl.readLock();
    private static final ReentrantReadWriteLock.WriteLock wl = rrwl.writeLock();
    private static Map<Class, PojoData> pojoDatas = new HashMap<>();

    public PojoData() {
        tableInfo = new TableInfo();
        columns = new HashMap<>();
    }

    public static PojoData forType(Class<?> type) {
        rl.lock();
        try {
            if (pojoDatas.containsKey(type))
                return pojoDatas.get(type);
        } finally {
            rl.unlock();
        }

        wl.lock();
        try {
            if (pojoDatas.containsKey(type))
                return pojoDatas.get(type);

            PojoData pd = new PojoData(type);
            pojoDatas.put(type, pd);
            return pd;
        } finally {
            wl.unlock();
        }
    }

    public PojoData(Class<?> type) {
        clazz = type;

        String tableName = "";
        String primaryKey = "id";
        boolean autoIncrement = true;

        if (clazz.isAnnotationPresent(TableName.class)) {
            TableName tableNameAnn = clazz.getAnnotation(TableName.class);

            if (tableNameAnn != null && tableNameAnn.value().length() > 0) {
                tableName = tableNameAnn.value();
            } else {
                tableName = clazz.getSimpleName();
            }
        }

        if (clazz.isAnnotationPresent(PrimaryKey.class)) {
            PrimaryKey primaryKeyAnn = clazz.getAnnotation(PrimaryKey.class);

            if (primaryKeyAnn != null) {
                autoIncrement = primaryKeyAnn.autoIncrement();
                if (primaryKeyAnn.value().length() > 0) {
                    primaryKey = primaryKeyAnn.value();
                }
            }
        }

        tableInfo = new TableInfo();
        tableInfo.setPrimaryKey(primaryKey);
        tableInfo.setTableName(tableName);
        tableInfo.setAutoIncrement(autoIncrement);

        columns = new HashMap<String, PojoColumn>();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Ignore.class))
                continue;

            String columnName = "";
            if (field.isAnnotationPresent(Column.class)) {
                Column columnAnn = field.getAnnotation(Column.class);
                if (columnAnn != null && columnAnn.value().length() > 0) {
                    columnName = columnAnn.value();
                } else {
                    columnName = field.getName();
                }
            } else {
                columnName = field.getName();
            }

            PojoColumn pc = new PojoColumn();
            pc.setColumnName(columnName);
            pc.setField(field);

            columns.put(columnName.toLowerCase(), pc);
        }
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public Map<String, PojoColumn> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, PojoColumn> columns) {
        this.columns = columns;
    }
}
