package com.jfirer.jsql.model;

import com.jfirer.jsql.model.model.*;
import com.jfirer.jsql.model.support.SFunction;

import java.util.Collection;
import java.util.List;

public interface Model
{
    static UpdateModel update(Class<?> ckass)
    {
        return new UpdateModel(ckass);
    }

    static InsertModel insert(Class<?> ckass)
    {
        return new InsertModel(ckass);
    }

    static <T> InsertEntityModel insert(T entity)
    {
        return new InsertEntityModel(entity);
    }

    static DeleteModel deleteFrom(Class<?> ckass)
    {
        return new DeleteModel(ckass);
    }

    static <T> UpdateEntityModel update(T entity)
    {
        return new UpdateEntityModel(entity);
    }

    static <T> QueryModel select(SFunction<T, ?>... fns)
    {
        QueryModel model = new QueryModel();
        for (SFunction<?, ?> fn : fns)
        {
            model.addSelect(fn);
        }
        return model;
    }

    static <T> BatchInsertModel batchInsert(Collection<T> entities)
    {
        return new BatchInsertModel((Collection<Object>) entities);
    }

    static QueryModel selectAll()
    {
        return new QueryModel();
    }

    static QueryModel selectAll(Class<?> ckass)
    {
        return new QueryModel().from(ckass);
    }

    static <T> QueryModel selectAlias(SFunction<T, ?> fn, String asName)
    {
        return new QueryModel().selectAs(fn, asName);
    }

    static <T> QueryModel selectWithFunction(SFunction<T, ?> fn, String function, String asName)
    {
        return new QueryModel().addSelectWithFunction(fn, function, asName);
    }

    static <T> QueryModel selectWithFunction(SFunction<T, ?> fn, String function)
    {
        return selectWithFunction(fn, function, null);
    }

    static <T> QueryModel selectCount(SFunction<T, ?> fn)
    {
        return new QueryModel().selectCount(fn);
    }

    static QueryModel selectCount()
    {
        return new QueryModel().selectCount();
    }

    static QueryModel selectCount(Class<?> ckass)
    {
        return new QueryModel().from(ckass).selectCount();
    }

    record ModelResult(String sql, List<Object> paramValues)
    {
    }

    ModelResult getResult();

    default String findColumnName(SFunction<?, ?> fn)
    {
        throw new UnsupportedOperationException();
    }
}
