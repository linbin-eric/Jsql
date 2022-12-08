package com.jfirer.jsql.curd;

import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;

import java.sql.Connection;

public interface CurdOpSupport<T>
{
    void update(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection);

    void save(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection);

    String insertWithPkNull(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection);

    void insert(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection);

    int delete(Object pk, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection);

    T find(Object pk, LockMode mode, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection);

    T find(Object pk, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection);
}
