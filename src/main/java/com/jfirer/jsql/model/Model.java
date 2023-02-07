package com.jfirer.jsql.model;

import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.support.LockMode;
import com.jfirer.jsql.model.support.SFunction;

public interface Model
{
    static Model update(Class<?> ckass)
    {
        return new BaseModel(new BaseModel.Update(ckass));
    }

    static Model insert(Class<?> ckass)
    {
        return new BaseModel(new BaseModel.InsertInto(ckass));
    }

    static <T> Model insert(T entity)
    {
        return new BaseModel(new BaseModel.InsertIntoWithObject(entity));
    }

    static Model deleteFrom(Class<?> ckass)
    {
        return new BaseModel(new BaseModel.Delete(ckass));
    }

    static <T> Model save(T entity)
    {
        return new BaseModel(new BaseModel.SaveWithObject(entity));
    }

    static <T> Model update(T entity)
    {
        return new BaseModel(new BaseModel.UpdateWithObject(entity));
    }

    static <T> Model select(SFunction<T, ?>... fns)
    {
        BaseModel model = new BaseModel();
        for (SFunction<T, ?> fn : fns)
        {
            model.addSelect(fn);
        }
        return model;
    }

    static Model selectAll()
    {
        return new BaseModel();
    }

    static Model selectAll(Class<?> ckass)
    {
        BaseModel model = new BaseModel();
        model.selectAll(ckass);
        return model;
    }

    static <T> Model selectAlias(SFunction<T, ?> fn, String asName)
    {
        BaseModel model = new BaseModel();
        model.selectAs(fn, asName);
        return model;
    }

    static <T> Model selectWithFunction(SFunction<T, ?> fn, String function, String asName)
    {
        BaseModel model = new BaseModel();
        model.addSelectWithFunction(fn, function, asName);
        return model;
    }

    static <T> Model selectCount(SFunction<T, ?> fn)
    {
        BaseModel model = new BaseModel();
        model.selectCount(fn);
        return model;
    }

    static Model selectCount()
    {
        BaseModel model = new BaseModel();
        model.selectCount();
        return model;
    }

    default Model from(Class<?> ckass)
    {
        return fromAs(ckass, TableEntityInfo.parse(ckass).getTableName());
    }

    Model fromAs(Class<?> ckass, String asName);

    <T> Model addSelect(SFunction<T, ?> fns);

    <T> Model selectAs(SFunction<T, ?> fn, String asName);

    <T> Model addSelectWithFunction(SFunction<T, ?> fn, String function, String asName);

    <T> Model set(SFunction<T, ?> fn, Object value);

    <T> Model insert(SFunction<T, ?> fn, Object value);

    Model leftJoin(Class ckass);

    Model rightJoin(Class<?> ckass);

    Model fullJoin(Class<?> ckass);

    Model innerJoin(Class<?> ckass);

    <E, T> Model on(Param param);

    Model where(Param param);

    <T> Model orderBy(SFunction<T, ?> fn, boolean desc);

    <T> Model groupBy(SFunction<T, ?> fn);

    Model returnType(Class<?> ckass);

    Model lockMode(LockMode lockMode);

    Model page(Page page);

    Model limit(int size);

    BaseModel.ModelResult getResult();
}
