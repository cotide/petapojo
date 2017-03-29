package com.viviframework.petapojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 葛劲 on 2016/5/11 0011.
 */
public class Sql {

    private String _sql;
    private Object[] _args;
    private Sql _rhs;
    private String _sqlFinal;
    private Object[] _argsFinal;

    public Sql() {

    }


    public Sql(String sql, Object... params) {
        _sql = sql;
        _args = params;
    }

    public static Sql create() {
        return new Sql();
    }


    public Sql append(Sql sql) {
        if (_rhs != null) {
            _rhs.append(sql);
        } else {
            _rhs = sql;
        }

        return this;
    }

    public Sql append(String sql, Object... params) {
        return append(new Sql(sql, params));
    }

    public Sql where(String sql, Object... params) {
        return append(new Sql("WHERE " + sql, params));
    }

    public Sql whereIn(String sql, Object[] paras) {
        if (sql == null || sql.length() == 0)
            throw new PetaPojoException("sql error: must have 'IN' word");
        if (paras == null || paras.length == 0)
            throw new PetaPojoException("paras error");
        if (!sql.contains("?"))
            throw new PetaPojoException("sql error: must have ?");

        String temp = "";
        for (Object ignored : paras) {
            temp = temp + "?,";
        }
        temp = temp.substring(0, temp.length() - 1);
        sql = sql.replace("?", temp);
        return where(sql, paras);
    }

    public Sql select(String columns) {
        return append(new Sql("SELECT " + columns));
    }

    public Sql from(String tables) {
        return append(new Sql("FROM " + tables));
    }

    public Sql orderBy(String columns) {
        return append(new Sql("ORDER BY " + columns));
    }

    public Sql groupBy(String columns) {
        return append(new Sql("GROUP BY " + columns));
    }


    private void build() {
        if (_sqlFinal != null && _sqlFinal.length() > 0)
            return;

        StringBuilder sb = new StringBuilder();
        List<Object> args = new ArrayList<Object>();

        build(sb, args, null);

        _sqlFinal = sb.toString();
        _argsFinal = args.toArray();
    }

    private void build(StringBuilder sb, List<Object> args, Sql lhs) {
        if (_sql != null && _sql.length() > 0) {
            if (sb.length() > 0) {
                sb.append("\n");
            }

            String sql = _sql;
            if (is(lhs, "WHERE ") && is(this, "WHERE "))
                sql = "AND " + sql.substring(6);
            if (is(lhs, "ORDER BY ") && is(this, "ORDER BY "))
                sql = ", " + sql.substring(9);

            for (Object arg : _args) {
                args.add(arg);
            }
            sb.append(sql);
        }

        if (_rhs != null)
            _rhs.build(sb, args, this);
    }


    private static boolean is(Sql sql, String sqlType) {
        return sql != null
                && sql._sql != null
                && sql._sql.toLowerCase().startsWith(sqlType.toLowerCase());
    }

    public String getFinalSql() {
        build();
        return _sqlFinal;
    }

    public Object[] getFinalArgs() {
        build();
        return _argsFinal;
    }
}
