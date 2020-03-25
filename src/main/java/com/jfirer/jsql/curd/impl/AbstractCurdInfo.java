package com.jfirer.jsql.curd.impl;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.annotation.pkstrategy.PkGenerator;
import com.jfirer.jsql.curd.CurdInfo;
import com.jfirer.jsql.curd.LockMode;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.transfer.resultset.ResultSetTransfer;
import com.jfirer.jsql.transfer.resultset.impl.BeanTransfer;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractCurdInfo<T> implements CurdInfo<T>
{
    class SqlAndFieldEntry
    {
        String          sql;
        ValueAccessor[] valueAccessors;
    }

    private SqlAndFieldEntry insertEntry;
    private SqlAndFieldEntry deleteEntry;
    private SqlAndFieldEntry updateEntry;
    private SqlAndFieldEntry getEntry;
    private SqlAndFieldEntry lockInShareEntry;
    private SqlAndFieldEntry lockForUpdateEntry;
    SqlAndFieldEntry autoGeneratePkInsertEntry;
    private PkGenerator.Generator generator;
    private       PkMode            mode = PkMode.OTHER;
    private final ResultSetTransfer beanTransfer;
    private final Field             pkField;

    enum PkMode
    {
        STRING, INT, LONG, OTHER
    }

    AbstractCurdInfo(Class<T> ckass)
    {
        TableEntityInfo tableEntityInfo = TableEntityInfo.parse(ckass);
        generateInsertEntry(tableEntityInfo);
        generateDeleteEntry(tableEntityInfo);
        generateUpdateEntry(tableEntityInfo);
        generateGetEntry(tableEntityInfo);
        generateLockInShareEntry(tableEntityInfo);
        generateLockForUpdateEntry(tableEntityInfo);
        if ( tableEntityInfo.getPkInfo().getField().isAnnotationPresent(PkGenerator.class) )
        {
            generatePkGenerator(tableEntityInfo);
        }
        else
        {
            generateNative(tableEntityInfo);
        }
        beanTransfer = new BeanTransfer().initialize(ckass);
        pkField = tableEntityInfo.getPkInfo().getField();
        if ( pkField.getType() == String.class )
        {
            mode = PkMode.STRING;
        }
        else if ( pkField.getType() == Integer.class )
        {
            mode = PkMode.INT;
        }
        else if ( pkField.getGenericType() == Long.class )
        {
            mode = PkMode.LONG;
        }
    }

    protected abstract void generateNative(TableEntityInfo tableEntityInfo);

    private void generatePkGenerator(TableEntityInfo tableEntityInfo)
    {
        try
        {
            Field pkField = tableEntityInfo.getPkInfo().getField();
            generator = pkField.getAnnotation(PkGenerator.class).value().newInstance();
            StringBuilder cache = new StringBuilder();
            List<Field> list = new LinkedList<Field>();
            cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (").append(tableEntityInfo.getPkInfo().getColumnName()).append(',');
            concatNonPkColumnNames(tableEntityInfo, pkField, cache, list);
            cache.setLength(cache.length()-1);
            cache.append(") values (?,");
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
                cache.append("?,");
            }
            cache.setLength(cache.length()-1);
            cache.append(")");
            autoGeneratePkInsertEntry = new SqlAndFieldEntry();
            autoGeneratePkInsertEntry.sql = cache.toString();
            autoGeneratePkInsertEntry.valueAccessors = buildValueAccessors(list);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
    }

     void concatNonPkColumnNames(TableEntityInfo tableEntityInfo, Field pkField, StringBuilder cache, List<Field> list)
    {
        for (TableEntityInfo.ColumnInfo info : tableEntityInfo.getPropertyNameKeyMap().values())
        {
            Field field = info.getField();
            if ( field.equals(pkField) )
            {
                continue;
            }
            cache.append(info.getColumnName()).append(',');
            list.add(field);
        }
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

    private void generateLockForUpdateEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("select * from ").append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().getColumnName()).append("=? for update");
        lockForUpdateEntry = new SqlAndFieldEntry();
        lockForUpdateEntry.sql = cache.toString();
        lockForUpdateEntry.valueAccessors = buildValueAccessor(tableEntityInfo.getPkInfo().getField());
    }

    private ValueAccessor[] buildValueAccessor(Field field)
    {
        return new ValueAccessor[]{new ValueAccessor(field)};
    }

    private void generateLockInShareEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("select * from ").append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().getField()).append("=? lock in share mode");
        lockInShareEntry = new SqlAndFieldEntry();
        lockInShareEntry.sql = cache.toString();
        lockInShareEntry.valueAccessors = buildValueAccessor(tableEntityInfo.getPkInfo().getField());
    }

    private void generateGetEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("select  ");
        for (TableEntityInfo.ColumnInfo info : tableEntityInfo.getPropertyNameKeyMap().values())
        {
            cache.append(info.getColumnName()).append(',');
        }
        cache.setLength(cache.length()-1);
        cache.append(" from ");
        cache.append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().getColumnName()).append("=?");
        getEntry = new SqlAndFieldEntry();
        getEntry.sql = cache.toString();
        getEntry.valueAccessors = buildValueAccessor(tableEntityInfo.getPkInfo().getField());
    }

    private void generateUpdateEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("update ").append(tableEntityInfo.getTableName()).append(" set ");
        List<Field> list = new LinkedList<Field>();
        for (TableEntityInfo.ColumnInfo info : tableEntityInfo.getPropertyNameKeyMap().values())
        {
            cache.append(info.getColumnName()).append("=?,");
            list.add(info.getField());
        }
        cache.setLength(cache.length()-1);
        cache.append(" where ").append(tableEntityInfo.getPkInfo().getColumnName()).append("=?");
        list.add(tableEntityInfo.getPkInfo().getField());
        updateEntry = new SqlAndFieldEntry();
        updateEntry.sql = cache.toString();
        updateEntry.valueAccessors = buildValueAccessors(list);
    }

    private void generateDeleteEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        cache.append("delete from ").append(tableEntityInfo.getTableName()).append(" where ").append(tableEntityInfo.getPkInfo().getColumnName())//
                .append("=?");
        deleteEntry = new SqlAndFieldEntry();
        deleteEntry.sql = cache.toString();
        deleteEntry.valueAccessors = buildValueAccessor(tableEntityInfo.getPkInfo().getField());
    }

    private void generateInsertEntry(TableEntityInfo tableEntityInfo)
    {
        StringBuilder cache = new StringBuilder();
        List<Field> list = new LinkedList<Field>();
        cache.append("insert into ").append(tableEntityInfo.getTableName()).append(" (");
        for (TableEntityInfo.ColumnInfo columnInfo : tableEntityInfo.getPropertyNameKeyMap().values())
        {
            cache.append(columnInfo.getColumnName()).append(',');
            list.add(columnInfo.getField());
        }
        cache.setLength(cache.length()-1);
        cache.append(") values(");
        int size = list.size();
        for (int i = 0; i < size; i++)
        {
            cache.append("?,");
        }
        cache.setLength(cache.length()-1);
        cache.append(')');
        insertEntry = new SqlAndFieldEntry();
        insertEntry.sql = cache.toString();
        insertEntry.valueAccessors = buildValueAccessors(list);
    }

    @Override
    public String insert(T entity, List<Object> params)
    {
        return fillParamsAndReturnSql(entity, params, insertEntry);
    }

    private String fillParamsAndReturnSql(T entity, List<Object> params, SqlAndFieldEntry entry)
    {
        try
        {
            for (ValueAccessor field : entry.valueAccessors)
            {
                params.add(field.get(entity));
            }
            return entry.sql;
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @Override
    public String update(T entity, List<Object> params)
    {
        return fillParamsAndReturnSql(entity, params, updateEntry);
    }

    @Override
    public String find(Object pk, List<Object> params)
    {
        params.add(pk);
        return getEntry.sql;
    }

    @Override
    public String delete(Object pk, List<Object> params)
    {
        params.add(pk);
        return deleteEntry.sql;
    }

    @Override
    public String find(Object pk, LockMode mode, List<Object> params)
    {
        params.add(pk);
        if ( mode == LockMode.SHARE )
        {
            return lockInShareEntry.sql;
        }
        else if ( mode == LockMode.UPDATE )
        {
            return lockForUpdateEntry.sql;
        }
        else
        {
            throw new NullPointerException();
        }
    }

    @Override
    public String autoGeneratePkInsert(T entity, List<Object> params)
    {
        try
        {
            if ( generator != null )
            {
                params.add(generator.next());
            }
            for (ValueAccessor field : autoGeneratePkInsertEntry.valueAccessors)
            {
                params.add(field.get(entity));
            }
            return autoGeneratePkInsertEntry.sql;
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    @Override
    public void setPkValue(T entity, String pk)
    {
        try
        {
            switch (mode)
            {
                case INT:
                    pkField.set(entity, Integer.valueOf(pk));
                    break;
                case STRING:
                    pkField.set(entity, pk);
                    break;
                case LONG:
                    pkField.set(entity, Long.valueOf(pk));
                    break;
                case OTHER:
                    break;
                default:
                    break;
            }
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
    }

    @Override
    public ResultSetTransfer getBeanTransfer()
    {
        return beanTransfer;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        if ( generator != null )
        {
            generator.setSessionFactory(sessionFactory);
        }
    }
}
