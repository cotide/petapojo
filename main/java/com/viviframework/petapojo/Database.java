package com.viviframework.petapojo;

import com.viviframework.petapojo.handlers.converters.ConvertException;
import com.viviframework.petapojo.page.PageInfo;
import org.springframework.jdbc.datasource.DataSourceUtils;
import com.viviframework.petapojo.handlers.DefaultListHandler;
import com.viviframework.petapojo.handlers.converters.Convert;
import com.viviframework.petapojo.handlers.converters.Converter;
import com.viviframework.petapojo.page.PageQueryInfo;
import com.viviframework.petapojo.pojo.PojoColumn;
import com.viviframework.petapojo.pojo.PojoData;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


public class Database {

    private DataSource dataSource;
    private Connection sharedConnection;
    private int sharedCOnnectionDepth;

    /**
     * 构造函数
     *
     * @param dataSource 传入的数据源
     */
    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
        sharedCOnnectionDepth = 0;
    }

    /**
     * 新增
     *
     * @param object POJO对象
     * @return 影响行数
     */
    public Object insert(Object object) {
        PojoData pd = PojoData.forType(object.getClass());
        return insert(pd.getTableInfo().getTableName(), pd.getTableInfo().getPrimaryKey(), pd.getTableInfo().isAutoIncrement(), object);
    }

    /**
     * 新增
     *
     * @param tableName     表名
     * @param primaryKey    主键名
     * @param autoIncrement 是否自增
     * @param object        POJO对象
     * @return 影响行数
     */
    private Object insert(String tableName, String primaryKey, boolean autoIncrement, Object object) {
        try {
            openConnection();
            PreparedStatement statement = null;
            ResultSet rs = null;
            PojoData pd = PojoData.forType(object.getClass());

            try {
                StringBuilder names = new StringBuilder();
                StringBuilder values = new StringBuilder();
                List<Object> args = new ArrayList<>();

                for (Map.Entry<String, PojoColumn> entry : pd.getColumns().entrySet()) {
                    if (autoIncrement && primaryKey != null && primaryKey.length() > 0 && primaryKey.equalsIgnoreCase(entry.getKey()))
                        continue;

                    names.append(entry.getKey()).append(",");
                    values.append("?,");
                    args.add(entry.getValue().getValue(object));
                }

                String sql = String.format("INSERT INTO %s (%s) VALUES (%s);",
                        tableName,
                        names.substring(0, names.length() - 1),
                        values.substring(0, values.length() - 1));
                statement = createPreparedStatement(autoIncrement, sql, args.toArray());
                int row = statement.executeUpdate();

                if (autoIncrement) {
                    rs = statement.getGeneratedKeys();
                    rs.next();
                    Object key = rs.getObject(1);

                    if (pd.getColumns().containsKey(primaryKey.toLowerCase())) {
                        PojoColumn pc = pd.getColumns().get(primaryKey.toLowerCase());
                        if (pc != null) {
                            pc.setValue(object, key);
                        }
                    }
                }
                return row;
            } finally {
                if (rs != null)
                    rs.close();

                if (statement != null)
                    statement.close();
            }
        } catch (Exception ex) {
            throw new PetaPojoException("insert error", ex);
        } finally {
            closeConnection();
        }
    }


    /**
     * 修改
     *
     * @param pojo POJO对象
     * @return 影响行数
     */
    public int update(Object pojo) {
        return update(pojo, null, null);
    }

    /**
     * 修改
     *
     * @param pojo            POJO对象
     * @param primaryKeyValue 主键值
     * @param columns         需要修改的列名
     * @return 影响行数
     */
    private int update(Object pojo, Object primaryKeyValue, List<String> columns) {
        PojoData pd = PojoData.forType(pojo.getClass());
        return update(pd.getTableInfo().getTableName(), pd.getTableInfo().getPrimaryKey(), pojo, primaryKeyValue, columns);
    }

    /**
     * 修改
     *
     * @param tableName       表名
     * @param primaryKey      主键名
     * @param pojo            POJO对象
     * @param primaryKeyValue 主键值
     * @param columns         需要修改的列名
     * @return 影响行数
     */
    private int update(String tableName, String primaryKey, Object pojo, Object primaryKeyValue, List<String> columns) {
        StringBuffer sb = new StringBuffer();
        List<Object> args = new ArrayList<>();
        PojoData pd = PojoData.forType(pojo.getClass());

        if (columns == null || columns.size() == 0) {
            for (Map.Entry<String, PojoColumn> entry : pd.getColumns().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(primaryKey))
                    continue;

                sb.append(String.format("%s = ? ,", entry.getKey()));
                args.add(entry.getValue().getValue(pojo));
            }
        } else {
            for (String column : columns) {
                if (column.equalsIgnoreCase(primaryKey))
                    continue;

                if (!pd.getColumns().containsKey(column.toLowerCase()))
                    continue;

                PojoColumn pc = pd.getColumns().get(column.toLowerCase());

                sb.append(String.format("%s = ? ,", pc.getColumnName()));
                args.add(pc.getValue(pojo));
            }
        }

        if (primaryKeyValue == null) {
            primaryKeyValue = pd.getColumns().get(primaryKey.toLowerCase()).getValue(pojo);
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s = ?",
                tableName, sb.substring(0, sb.length() - 1), primaryKey);
        args.add(primaryKeyValue);

        return executeUpdate(sql, args.toArray());
    }


    /**
     * 执行SQL查询
     *
     * @param sql  SQL语句
     * @param args 参数列表
     * @return 影响行数
     */
    public int executeUpdate(String sql, Object... args) {
        try {
            openConnection();
            PreparedStatement statement = null;
            try {
                statement = createPreparedStatement(false, sql, args);
                int row = statement.executeUpdate();
                return row;
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }
        } catch (SQLException ex) {
            throw new PetaPojoException("executeUpdate error", ex);
        } finally {
            closeConnection();
        }
    }


    /**
     * 删除
     *
     * @param pojo POJO对象
     * @return 影响行数
     */
    public int delete(Object pojo) {
        PojoData pd = PojoData.forType(pojo.getClass());
        return delete(pd.getTableInfo().getTableName(), pd.getTableInfo().getPrimaryKey(), pojo);
    }

    /**
     * 删除
     *
     * @param tableName  表名
     * @param primaryKey 主键名
     * @param pojo       POJO对象
     * @return 影响行数
     */
    private int delete(String tableName, String primaryKey, Object pojo) {
        return delete(tableName, primaryKey, pojo, null);
    }

    /**
     * 删除
     *
     * @param tableName       表名
     * @param primaryKey      主键名
     * @param pojo            POJO对象
     * @param primaryKeyValue 主键值
     * @return 影响行数
     */
    private int delete(String tableName, String primaryKey, Object pojo, Object primaryKeyValue) {
        if (primaryKeyValue == null) {
            PojoData pd = PojoData.forType(pojo.getClass());
            if (pd.getColumns().containsKey(primaryKey.toLowerCase()))
                primaryKeyValue = pd.getColumns().get(primaryKey.toLowerCase()).getValue(pojo);
        }

        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, primaryKey);
        return executeUpdate(sql, primaryKeyValue);
    }

    /**
     * 删除
     *
     * @param type 类型
     * @param sql  SQL
     * @param <T>  泛型
     * @return 影响行数
     */
    public <T> int delete(Class<T> type, Sql sql) {
        return delete(type, sql.getFinalSql(), sql.getFinalArgs());
    }

    /**
     * 删除
     *
     * @param type 类型
     * @param sql  SQL语句
     * @param args 参数列表
     * @param <T>  泛型
     * @return 影响行数
     */
    public <T> int delete(Class<T> type, String sql, Object... args) {
        PojoData pd = PojoData.forType(type);
        sql = String.format("DELETE FROM %s %s", pd.getTableInfo().getTableName(), sql);
        return executeUpdate(sql, args);
    }

    /**
     * 删除
     *
     * @param type             类型
     * @param pojoOrPrimaryKey 主键值
     * @param <T>              泛型
     * @return 影响行数
     */
    public <T> int delete(Class<T> type, Object pojoOrPrimaryKey) {
        if (type.equals(pojoOrPrimaryKey.getClass())) {
            return delete(pojoOrPrimaryKey);
        }

        PojoData pd = PojoData.forType(type);
        return delete(pd.getTableInfo().getTableName(), pd.getTableInfo().getPrimaryKey(), null, pojoOrPrimaryKey);
    }

    /**
     * 查询一个对象
     *
     * @param type 类型
     * @param sql  SQL
     * @param <T>  泛型
     * @return 对象
     */
    public <T> T firstOrDefault(Class<T> type, Sql sql) {
        return firstOrDefault(type, sql.getFinalSql(), sql.getFinalArgs());
    }

    /**
     * 查询一个对象
     *
     * @param type 类型
     * @param sql  SQL
     * @param args 参数列表
     * @param <T>  泛型
     * @return 对象
     */
    public <T> T firstOrDefault(Class<T> type, String sql, Object... args) {
        List<T> list = query(type, sql, args);
        if (list != null && list.size() > 0)
            return list.get(0);

        return null;
    }

    /**
     * 查询一个列表
     *
     * @param type 类型
     * @param sql  SQL
     * @param <T>  泛型
     * @return 列表
     */
    public <T> List<T> query(Class<T> type, Sql sql) {
        return query(type, sql.getFinalSql(), sql.getFinalArgs());
    }

    /**
     * 查询一个列表
     *
     * @param type 类型
     * @param sql  SQL
     * @param args 参数列表
     * @param <T>  泛型
     * @return 列表
     */
    public <T> List<T> query(Class<T> type, String sql, Object... args) {
        try {
            openConnection();
            PreparedStatement statement = null;
            ResultSet rs = null;

            try {
                sql = addSelectClause(type, sql);
                statement = createPreparedStatement(false, sql, args);
                rs = statement.executeQuery();
                DefaultListHandler<T> handler = new DefaultListHandler<>(type);
                return handler.handle(rs);
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }

                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        } catch (SQLException ex) {
            throw new PetaPojoException(ex);
        } finally {
            closeConnection();
        }
    }


    /**
     * 分页查询
     *
     * @param type      类型
     * @param pageIndex 页索引
     * @param pageSize  页大小
     * @param sql       SQL
     * @param <T>       泛型
     * @return 分页列表
     */
    public <T> PageInfo<T> pagedList(Class<T> type, int pageIndex, int pageSize, Sql sql) {
        return pagedList(type, pageIndex, pageSize, sql.getFinalSql(), sql.getFinalArgs());
    }

    /**
     * 分页查询
     *
     * @param type      类型
     * @param pageIndex 页索引
     * @param pageSize  页大小
     * @param sql       SQL
     * @param args      参数列表
     * @param <T>       泛型
     * @return 分页列表
     */
    public <T> PageInfo<T> pagedList(Class<T> type, int pageIndex, int pageSize, String sql, Object... args) {
        sql = addSelectClause(type, sql);
        PageQueryInfo queryInfo = buildPagingQueris((pageIndex - 1) * pageSize, pageSize, sql);

        int count = executeScalar(int.class, queryInfo.getCountSql(), args);

        PageInfo<T> result = new PageInfo<>();
        result.setCurrentPage(pageIndex);
        result.setTotalItems(count);
        result.setItemsPrePage(pageSize);
        result.setTotalPages(count / pageSize + (count % pageSize != 0 ? 1 : 0));
        result.setItems(query(type, queryInfo.getPageSql(), args));

        return result;
    }

    /**
     * 获取第一行第一列的泛型对象
     *
     * @param type 类型
     * @param sql  SQL
     * @param <T>  泛型
     * @return 泛型对象
     */
    public <T> T executeScalar(Class<T> type, Sql sql) {
        return executeScalar(type, sql.getFinalSql(), sql.getFinalArgs());
    }

    /**
     * 获取第一行第一列的泛型对象
     *
     * @param type 类型
     * @param sql  SQL
     * @param args 参数列表
     * @param <T>  泛型
     * @return 泛型对象
     */
    public <T> T executeScalar(Class<T> type, String sql, Object... args) {
        try {
            Converter<T> converter = Convert.getConverterIfExists(type);
            return converter.convert(executeScalar(sql, args));
        } catch (ConvertException e) {
            throw new PetaPojoException("convert type error", e);
        }
    }

    /**
     * 获取第一行第一列的对象
     *
     * @param sql  SQL
     * @param args 参数列表
     * @return 对象
     */
    public Object executeScalar(String sql, Object... args) {
        try {
            openConnection();
            PreparedStatement statement = null;
            ResultSet rs = null;
            try {
                statement = createPreparedStatement(false, sql, args);
                rs = statement.executeQuery();
                if (rs.next())
                    return rs.getObject(1);
                return null;
            } finally {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        } catch (SQLException ex) {
            throw new PetaPojoException("executeScalar error", ex);
        } finally {
            closeConnection();
        }
    }

    /**
     * 调用存储过程
     * @param sql SQL
     * @param args 输入参数
     * @return 是否执行完成
     */
    public void callProcedure(String sql, Object... args) {
        try {
            openConnection();
            CallableStatement statement = null;
            try {
                statement = createCallableStatement(sql, args);
                statement.execute();
            } finally {
                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        } catch (SQLException e) {
            throw new PetaPojoException("callProcedure error: " + e.getMessage(), e);
        } finally {
            closeConnection();
        }
    }

    /**
     * 打开数据库连接
     */
    private void openConnection() {
        try {
            if (sharedCOnnectionDepth == 0) {
                sharedConnection = DataSourceUtils.getConnection(dataSource);
                sharedCOnnectionDepth++;
            }
        } catch (Exception ex) {
            throw new PetaPojoException("open connection error", ex);
        }
    }

    /**
     * 关闭数据库连接
     */
    private void closeConnection() {
        try {
            if (sharedCOnnectionDepth > 0) {
                sharedCOnnectionDepth--;
                if (sharedCOnnectionDepth == 0 && sharedConnection != null) {
                    DataSourceUtils.doReleaseConnection(sharedConnection, dataSource);
                }
            }
        } catch (SQLException ex) {
            throw new PetaPojoException("close connection error", ex);
        }
    }

    /**
     * 构造一个PreparedStatement
     *
     * @param isReturnGenerateKeys 是否返回自增健值
     * @param sql                  SQL
     * @param args                 参数列表
     * @return PreparedStatement
     */
    private PreparedStatement createPreparedStatement(boolean isReturnGenerateKeys, String sql, Object... args) {
        try {
            PreparedStatement statement = isReturnGenerateKeys
                    ? sharedConnection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
                    : sharedConnection.prepareStatement(sql);
            ParameterMetaData metaData = statement.getParameterMetaData();
            if (metaData.getParameterCount() != args.length)
                throw new PetaPojoException("error:parameter count mst equal ? count");

            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg == null) {
                        statement.setObject(i + 1, null);
                    } else {
                        Converter converter = Convert.getConverterIfExists(arg.getClass());
                        statement.setObject(i + 1, converter.convert(arg));
                    }
                }
            }

            return statement;
        } catch (SQLException | ConvertException e) {
            throw new PetaPojoException("createPreparedStatement error", e);
        }
    }

    private CallableStatement createCallableStatement(String sql, Object[] args) {
        try {
            CallableStatement statement = sharedConnection.prepareCall(sql);
            ParameterMetaData metaData = statement.getParameterMetaData();
            if (metaData.getParameterCount() != args.length)
                throw new PetaPojoException("error:parameter count mst equal ? count");

            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    Converter converter = Convert.getConverterIfExists(arg.getClass());
                    statement.setObject(i + 1, converter.convert(arg));
                }
            }

            return statement;
        } catch (SQLException | ConvertException e) {
            throw new PetaPojoException("createCallableStatement error:" + e.getMessage(), e);
        }
    }


    private final static Pattern selectPattern = Pattern.compile("\\s*(SELECT|EXECUTE|CALL)\\s", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
    private final static Pattern fromPattern = Pattern.compile("\\s*FROM\\s", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * 自动增加SELECT/FROM语句，以及列名
     *
     * @param type 类型
     * @param sql  SQL
     * @param <T>  泛型
     * @return SQL
     */
    private <T> String addSelectClause(Class<T> type, String sql) {
        if (sql.startsWith(";"))
            return sql.substring(1);

        Matcher selectMatcher = selectPattern.matcher(sql);
        Matcher fromMatcher = fromPattern.matcher(sql);

        if (!selectMatcher.find()) {
            PojoData pd = PojoData.forType(type);
            StringBuffer cols = new StringBuffer();
            for (Map.Entry<String, PojoColumn> entry : pd.getColumns().entrySet()) {
                cols.append(entry.getValue().getColumnName()).append(",");
            }

            if (fromMatcher.find()) {
                sql = String.format("SELECT %s %s", cols.substring(0, cols.length() - 1), sql);
            } else {
                sql = String.format("SELECT %s FROM %s %s", cols.substring(0, cols.length() - 1), pd.getTableInfo().getTableName(), sql);
            }
        }

        return sql;
    }


    private final static Pattern PATTERN_BRACKET = Pattern.compile("(\\(|\\)|[^\\(\\)]*)");
    private final static Pattern PATTERN_SELECT = Pattern.compile("select([\\W\\w]*)from", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS | Pattern.MULTILINE);
    private final static Pattern PATTERN_DISTINCT = Pattern.compile("\\ADISTINCT\\s", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS | Pattern.MULTILINE);

    /**
     * 组装分页SQL
     *
     * @param skip skip
     * @param take 提取的条数
     * @param sql  SQL
     * @return 分页SQL(获取总数的SQL，获取列表的SQL)
     */
    private PageQueryInfo buildPagingQueris(long skip, int take, String sql) {
        // 匹配所有括号
        Matcher matcherBracket = PATTERN_BRACKET.matcher(sql);
        String sqlReplace = sql;
        if (matcherBracket.find()) {
            matcherBracket.reset();
            List<String> tempList = new ArrayList<>();
            while (matcherBracket.find()) {
                tempList.add(matcherBracket.group());
            }

            List<String> copyList = new ArrayList<>();
            tempList.forEach(copyList::add);

            while (true) {
                int endIndex = IntStream.range(0, copyList.size())
                        .filter(i -> copyList.get(i).contains(")"))
                        .findFirst()
                        .orElse(-1);
                if (endIndex < 0)
                    break;

                int startIndex = IntStream.range(0, endIndex)
                        .filter(i -> copyList.get(i).contains("("))
                        .reduce((a, b) -> b)
                        .orElse(-1);
                if (startIndex < 0)
                    break;

                IntStream.range(startIndex, endIndex + 1)
                        .forEach(i -> copyList.set(i, ""));
            }

            int start = IntStream.range(0, copyList.size())
                    .filter(i -> copyList.get(i).toLowerCase().contains("select"))
                    .findFirst()
                    .orElse(-1);
            int end = IntStream.range(0, copyList.size())
                    .filter(i -> copyList.get(i).toLowerCase().contains("from"))
                    .findFirst()
                    .orElse(-1);
            if (start < 0 || end < 0)
                throw new PetaPojoException("build paging querySql error:no select or no from");

            sqlReplace = "";
            for (int i = start; i <= end; i++) {
                sqlReplace += tempList.get(i);
            }
        }

        Matcher matcherSelect = PATTERN_SELECT.matcher(sqlReplace);
        if (!matcherSelect.find()) {
            throw new PetaPojoException("build paging querySql error:canot find select from");
        }

        String sqlSelectCols = null;
        int colsStartIndex = -1;
        int colsEndIndex = -1;
        sqlSelectCols = matcherSelect.group(1);
        colsStartIndex = matcherSelect.start(1);
        colsEndIndex = matcherSelect.end(1);

        String countSql = "";
        Matcher matcherDistinct = PATTERN_DISTINCT.matcher(sqlSelectCols);
        if (matcherDistinct.find()) {
            countSql = String.format("%s COUNT(%s) %s", sql.substring(0, colsStartIndex), sqlSelectCols, sql.substring(colsEndIndex));
        } else {
            countSql = String.format("%s COUNT(1) %s", sql.substring(0, colsStartIndex), sql.substring(colsEndIndex));
        }

        String pageSql = String.format("%s LIMIT %s OFFSET %s", sql, take, skip);

        PageQueryInfo queryInfo = new PageQueryInfo();
        queryInfo.setPageSql(pageSql);
        queryInfo.setCountSql(countSql);
        return queryInfo;
    }

    public <T> T getById(Class<T> type, Object primaryKey) {
        PojoData pd = PojoData.forType(type);
        String sql = String.format("WHERE %s = ? ", pd.getTableInfo().getPrimaryKey());
        return firstOrDefault(type, sql, primaryKey);
    }

    public <T> int bulkInsertRecords(List<T> tempList) {
        if (null == tempList || tempList.size() == 0)
            return 0;

        try {
            openConnection();
            PreparedStatement statement = null;
            PojoData pd = PojoData.forType(tempList.get(0).getClass());

            Boolean autoIncrement = pd.getTableInfo().isAutoIncrement();
            String primaryKey = pd.getTableInfo().getPrimaryKey();
            String tableName = pd.getTableInfo().getTableName();

            try {
                StringBuilder names = new StringBuilder();
                StringBuilder columnsValues = new StringBuilder();

                for (Map.Entry<String, PojoColumn> entry : pd.getColumns().entrySet()) {
                    if (autoIncrement && primaryKey != null && primaryKey.length() > 0 && primaryKey.equalsIgnoreCase(entry.getKey()))
                        continue;
                    names.append(entry.getKey()).append(",");
                    columnsValues.append("?,");
                }

                StringBuilder values = new StringBuilder();
                List<Object> args = new ArrayList<>();

                for (T aTempList : tempList) {
                    values.append(String.format("(%s),", columnsValues.substring(0, columnsValues.length() - 1)));
                    for (Map.Entry<String, PojoColumn> entry : pd.getColumns().entrySet()) {
                        if (autoIncrement && primaryKey != null && primaryKey.length() > 0 && primaryKey.equalsIgnoreCase(entry.getKey()))
                            continue;
                        args.add(entry.getValue().getValue(aTempList));
                    }
                }

                String sql = String.format("INSERT INTO %s (%s) VALUES %s;",
                        tableName,
                        names.substring(0, names.length() - 1),
                        values.substring(0, values.length() - 1));
                statement = createPreparedStatement(autoIncrement, sql, args.toArray());
                int row = statement.executeUpdate();

                return row;

            } finally {
                if (statement != null)
                    statement.close();
            }
        } catch (Exception ex) {
            throw new PetaPojoException("bulkInsertRecords error", ex);
        } finally {
            closeConnection();
        }
    }
}
