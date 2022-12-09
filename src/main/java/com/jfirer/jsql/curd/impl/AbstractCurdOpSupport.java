package com.jfirer.jsql.curd.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.jsql.annotation.PkGenerator;
import com.jfirer.jsql.curd.CurdOpSupport;
import com.jfirer.jsql.curd.LockMode;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.metadata.TableEntityInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCurdOpSupport<T> implements CurdOpSupport<T>
{
    private static ThreadLocal<List<Object>> cachedParam = ThreadLocal.withInitial(ArrayList::new);

    record SqlAndFieldAccessors(String sql, ValueAccessor[] valueAccessors) {}

    record StringAndValueAccessors(String sqlSegment, List<ValueAccessor> list) {}

    private SqlAndFieldAccessors  insertEntry;
    private SqlAndFieldAccessors  insertWithPkNullEntry;
    private SqlAndFieldAccessors  deleteEntry;
    private SqlAndFieldAccessors  updateEntry;
    private SqlAndFieldAccessors  getEntry;
    private SqlAndFieldAccessors  lockInShareEntry;
    private SqlAndFieldAccessors  lockForUpdateEntry;
    private PkGenerator.Generator generator;
    private PkValueType           pkValueType;
    private PkGenerateMode        pkGenerateMode;
    private ValueAccessor         pkAccessor;
    private Class<?>              ckass;

    enum PkGenerateMode
    {
        SET_BY_ANNOTATION,
        NAVIVE
    }

    enum PkValueType
    {
        STRING,
        INT,
        LONG
    }

    AbstractCurdOpSupport(Class<T> ckass)
    {
        this.ckass = ckass;
        TableEntityInfo tableEntityInfo = TableEntityInfo.parse(ckass);
        Field           pkField         = tableEntityInfo.getPkInfo().field();
        pkValueType = detectPkValueType(pkField);
        pkAccessor = new ValueAccessor(pkField);
        if (pkField.isAnnotationPresent(PkGenerator.class))
        {
            pkGenerateMode = PkGenerateMode.SET_BY_ANNOTATION;
            //如果采用外部注解类来生成主键，其等于全量数据插入，也就等同于insert。因此这个时候，insertWithPkNullEntry就应该为null。
            insertWithPkNullEntry = null;
            try
            {
                generator = pkField.getAnnotation(PkGenerator.class).value().getDeclaredConstructor().newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                   NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            pkGenerateMode = PkGenerateMode.NAVIVE;
            insertWithPkNullEntry = generateInsertWithPkNullEntry(tableEntityInfo);
        }
        insertEntry = generateInsertEntry(tableEntityInfo);
        updateEntry = generateUpdateEntry(tableEntityInfo);
        getEntry = generateGetEntry(tableEntityInfo);
        deleteEntry = new SqlAndFieldAccessors("delete from " + tableEntityInfo.getTableName() + " where " + tableEntityInfo.getPkInfo().columnName() + "=?", buildValueAccessor(pkAccessor.getField()));
        lockInShareEntry = new SqlAndFieldAccessors("select * from " + tableEntityInfo.getTableName() + " where " + tableEntityInfo.getPkInfo().columnName() + "=? lock in share mode", buildValueAccessor(pkAccessor.getField()));
        lockForUpdateEntry = new SqlAndFieldAccessors("select * from " + tableEntityInfo.getTableName() + " where " + tableEntityInfo.getPkInfo().columnName() + "=? for update", buildValueAccessor(pkAccessor.getField()));
    }

    private PkValueType detectPkValueType(Field pkField)
    {
        if (pkField.getType() == String.class)
        {
            return PkValueType.STRING;
        }
        else if (pkField.getType() == Integer.class)
        {
            return PkValueType.INT;
        }
        else if (pkField.getGenericType() == Long.class)
        {
            return PkValueType.LONG;
        }
        else
        {
            throw new IllegalArgumentException("不支持非String，Integer，Long以外类型的主键");
        }
    }

    protected abstract SqlAndFieldAccessors generateInsertWithPkNullEntry(TableEntityInfo tableEntityInfo);

    @Override
    public void save(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection)
    {
        Object pk = pkAccessor.get(entity);
        if (pk == null)
        {
            switch (pkGenerateMode)
            {
                case SET_BY_ANNOTATION ->
                {
                    pk = generator.next();
                    pkAccessor.setObject(entity, pk);
                    insert(entity, headSqlExecutor, dialect, connection);
                }
                case NAVIVE ->
                {
                    String s = insertWithPkNull(entity, headSqlExecutor, dialect, connection);
                    switch (pkValueType)
                    {
                        case STRING -> pkAccessor.setObject(entity, s);
                        case INT -> pkAccessor.set(entity, Integer.valueOf(s));
                        case LONG -> pkAccessor.set(entity, Long.valueOf(s));
                    }
                }
            }
        }
        else
        {
            update(entity, headSqlExecutor, dialect, connection);
        }
    }

    @Override
    public String insertWithPkNull(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection)
    {
        List<Object> params = cachedParam.get();
        try
        {
            fillParamsAndReturnSql(entity, params, insertWithPkNullEntry);
            String pk = headSqlExecutor.insertWithReturnKey(insertWithPkNullEntry.sql, params, connection, dialect);
            return pk;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            params.clear();
        }
    }

    @Override
    public void insert(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection)
    {
        insertOrUpdate(entity, headSqlExecutor, dialect, connection, insertEntry);
    }

    StringAndValueAccessors concatNonPkColumnNames(TableEntityInfo tableEntityInfo)
    {
        Field               pkField = tableEntityInfo.getPkInfo().field();
        List<ValueAccessor> list    = new ArrayList<>();
        String colunmNames = tableEntityInfo.getPropertyNameKeyMap().values().stream()//
                                            .filter(columnInfo -> columnInfo.field() != pkField)//
                                            .peek(columnInfo -> list.add(new ValueAccessor(columnInfo.field())))//
                                            .map(columnInfo -> columnInfo.columnName())//
                                            .collect(Collectors.joining(","));
        return new StringAndValueAccessors(colunmNames, list);
    }

    ValueAccessor[] buildValueAccessors(List<Field> list)
    {
        ValueAccessor[] valueAccessors = new ValueAccessor[list.size()];
        for (int i = 0; i < valueAccessors.length; i++)
        {
            valueAccessors[i] = new ValueAccessor(list.get(i));
        }
        return valueAccessors;
    }

    private ValueAccessor[] buildValueAccessor(Field field)
    {
        return new ValueAccessor[]{new ValueAccessor(field)};
    }

    private SqlAndFieldAccessors generateGetEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("select  ");
        String segment = tableEntityInfo.getPropertyNameKeyMap().values().stream().map(columnInfo -> columnInfo.columnName()).collect(Collectors.joining(","));
        cache.append(segment).append(" from ");
        cache.append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().columnName()).append("=?");
        return new SqlAndFieldAccessors(cache.toString(), new ValueAccessor[]{pkAccessor});
    }

    private SqlAndFieldAccessors generateUpdateEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("update ").append(tableEntityInfo.getTableName()).append(" set ");
        List<ValueAccessor> list = new ArrayList<>();
        String segment = tableEntityInfo.getPropertyNameKeyMap().values().stream()//
                                        .peek(columnInfo -> list.add(new ValueAccessor(columnInfo.field())))//
                                        .map(columnInfo -> columnInfo.columnName() + "=?")//
                                        .collect(Collectors.joining(","));
        cache.append(segment).append(" where ").append(tableEntityInfo.getPkInfo().columnName()).append("=?");
        list.add(new ValueAccessor(tableEntityInfo.getPkInfo().field()));
        return new SqlAndFieldAccessors(cache.toString(), list.toArray(new ValueAccessor[0]));
    }

    private SqlAndFieldAccessors generateInsertEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder       cache = new StringBuilder();
        List<ValueAccessor> list  = new LinkedList<>();
        cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
        String segment = tableEntityInfo.getPropertyNameKeyMap().values().stream()//
                                        .peek(columnInfo -> list.add(new ValueAccessor(columnInfo.field())))//
                                        .map(columnInfo -> columnInfo.columnName()).collect(Collectors.joining(","));
        cache.append(segment).append(") values(");
        segment= Stream.generate(()->"?").limit(list.size()).collect(Collectors.joining(","));
        cache.append(segment).append(")");
        return new SqlAndFieldAccessors(cache.toString(), list.toArray(new ValueAccessor[0]));
    }

    private String fillParamsAndReturnSql(T entity, List<Object> params, SqlAndFieldAccessors entry)
    {
        try
        {
            for (ValueAccessor field : entry.valueAccessors)
            {
                params.add(field.get(entity));
            }
            return entry.sql;
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @Override
    public void update(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection)
    {
        insertOrUpdate(entity, headSqlExecutor, dialect, connection, updateEntry);
    }

    private void insertOrUpdate(T entity, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection, SqlAndFieldAccessors entry)
    {
        List<Object> params = cachedParam.get();
        try
        {
            fillParamsAndReturnSql(entity, params, entry);
            headSqlExecutor.update(entry.sql, params, connection, dialect);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            params.clear();
        }
    }

    @Override
    public T find(Object pk, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection)
    {
        List<Object> param = cachedParam.get();
        try
        {
            param.add(pk);
            return (T) headSqlExecutor.queryOne(getEntry.sql, ckass, param, connection, dialect);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            param.clear();
        }
    }

    @Override
    public int delete(Object pk, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection)
    {
        List<Object> param = cachedParam.get();
        try
        {
            param.add(pk);
            return headSqlExecutor.update(deleteEntry.sql, param, connection, dialect);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            param.clear();
        }
    }

    @Override
    public T find(Object pk, LockMode mode, SqlExecutor headSqlExecutor, Dialect dialect, Connection connection)
    {
        List<Object> param = cachedParam.get();
        param.add(pk);
        try
        {
            switch (mode)
            {
                case SHARE ->
                {
                    return (T) headSqlExecutor.queryOne(lockInShareEntry.sql, ckass, param, connection, dialect);
                }
                case UPDATE ->
                {
                    return (T) headSqlExecutor.queryOne(lockForUpdateEntry.sql, ckass, param, connection, dialect);
                }
                default -> throw new IllegalArgumentException();
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            param.clear();
        }
    }
}
