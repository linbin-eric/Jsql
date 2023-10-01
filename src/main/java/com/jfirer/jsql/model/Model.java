package com.jfirer.jsql.model;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.model.*;
import com.jfirer.jsql.model.support.SFunction;

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

    static <T> BatchInsertModel batchInsert(List<T> entities)
    {
        return new BatchInsertModel((List<Object>) entities);
    }

    static QueryModel selectAll()
    {
        return new QueryModel();
    }

    static QueryModel selectAll(Class<?> ckass)
    {
        QueryModel model = new QueryModel();
        model.from(ckass);
        return model;
    }

    static <T> QueryModel selectAlias(SFunction<T, ?> fn, String asName)
    {
        QueryModel model = new QueryModel();
        model.selectAs(fn, asName);
        return model;
    }

    static <T> QueryModel selectWithFunction(SFunction<T, ?> fn, String function, String asName)
    {
        QueryModel model = new QueryModel();
        model.addSelectWithFunction(fn, function, asName);
        return model;
    }

    static <T> QueryModel selectWithFunction(SFunction<T, ?> fn, String function)
    {
        return selectWithFunction(fn, function, null);
    }

    static <T> QueryModel selectCount(SFunction<T, ?> fn)
    {
        QueryModel model = new QueryModel();
        model.selectCount(fn);
        return model;
    }

    static QueryModel selectCount()
    {
        QueryModel model = new QueryModel();
        model.selectCount();
        return model;
    }

    static QueryModel selectCount(Class<?> ckass)
    {
        QueryModel model = new QueryModel();
        model.from(ckass);
        model.selectCount();
        return model;
    }

    record ModelResult(String sql, List<Object> paramValues, Class returnType,
                       TableEntityInfo.PkReturnType pkReturnType)
    {
    }

    ModelResult getResult();

    default String findColumnName(SFunction<?, ?> fn)
    {
        throw new UnsupportedOperationException();
    }
}
