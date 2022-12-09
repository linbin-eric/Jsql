package com.jfirer.jsql.model;

import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.support.SFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Model
{
    Class<?>         entityClass;
    List<WhereEntry> whereEntries;
    private String generatedSql;

    Model()
    {
        // TODO Auto-generated constructor stub
    }

    class WhereEntry
    {
        final String propertyName;
        final Object value;

        WhereEntry(String propertyName, Object value)
        {
            this.propertyName = propertyName;
            this.value = value;
        }
    }

    /**
     * @param cache
     */
    void setWhereColumns(StringBuilder cache)
    {
        if (whereEntries != null)
        {
            cache.append(" where ");
            Map<String, TableEntityInfo.ColumnInfo> columnInfoMap = TableEntityInfo.parse(entityClass).getPropertyNameKeyMap();
            for (WhereEntry each : whereEntries)
            {
                String columnName = columnInfoMap.get(each.propertyName).columnName();
                cache.append(columnName).append("=? and ");
            }
            cache.setLength(cache.length() - 4);
        }
    }

    public Model insert(String property, Object value)
    {
        throw new UnsupportedOperationException();
    }

    public <T> Model insert(SFunction<T,?> function, Object value)
    {
        return insert(function.resolveFieldName(), value);
    }

    public Model select(String propertyName)
    {
        throw new UnsupportedOperationException();
    }

    public <T> Model select(SFunction<T,?> function)
    {
        return select(function.resolveFieldName());
    }

    public Model set(String property, Object value)
    {
        throw new UnsupportedOperationException();
    }

    public <T> Model set(SFunction<T,?> function, Object value)
    {
        return set(function.resolveFieldName(), value);
    }

    public Model orderBy(String propertyName, boolean desc)
    {
        throw new UnsupportedOperationException();
    }

    public <T> Model orderBy(SFunction<T,?> function, boolean desc)
    {
        return orderBy(function.resolveFieldName(), desc);
    }

    public Model setPage(Page page)
    {
        throw new UnsupportedOperationException();
    }

    Model from(Class<?> entityClass)
    {
        if (!entityClass.isAnnotationPresent(TableDef.class))
        {
            throw new IllegalArgumentException("没有实体类注解");
        }
        this.entityClass = entityClass;
        return this;
    }

    public Model where(String propertyName, Object value)
    {
        if (whereEntries == null)
        {
            whereEntries = new LinkedList<WhereEntry>();
        }
        whereEntries.add(new WhereEntry(propertyName, value));
        return this;
    }

    public <T> Model where(SFunction<T,?> function, Object value)
    {
        if (whereEntries == null)
        {
            whereEntries = new LinkedList<WhereEntry>();
        }
        whereEntries.add(new WhereEntry(SFunction.resolveFieldName(function), value));
        return this;
    }

    public Class<?> getEntityClass()
    {
        return entityClass;
    }

    public String getSql()
    {
        if (generatedSql != null)
        {
            return generatedSql;
        }
        generatedSql = _getSql();
        return generatedSql;
    }

    protected abstract String _getSql();

    public abstract List<Object> getParams();

    public static Model delete(Class<?> ckass)
    {
        return new DeleteModel().from(ckass);
    }

    public static Model query(Class<?> ckass)
    {
        return new QueryModel().from(ckass);
    }

    public static Model update(Class<?> ckass)
    {
        return new UpdateModel().from(ckass);
    }

    public static Model insert(Class<?> ckass)
    {
        return new InsertModel().from(ckass);
    }

    public static Model count(Class<?> ckass)
    {
        return new CountModel().from(ckass);
    }
}
