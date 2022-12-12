package com.jfirer.jsql.model;

import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.support.SFunction;

public interface Model
{
    static Model update(Class<?> ckass)
    {
        BaseModel model = new BaseModel();
        model.setUpdate(new BaseModel.Update(ckass));
        return model;
    }

    static Model insert(Class<?> ckass)
    {
        BaseModel model = new BaseModel();
        model.setInsertInto(new BaseModel.InsertInto(ckass));
        return model;
    }

    static Model deleteFrom(Class<?> ckass)
    {
        BaseModel model = new BaseModel();
        model.setDelete(new BaseModel.Delete(ckass));
        return model;
    }

    static Model from(Class<?> ckass)
    {
        return fromAs(ckass, TableEntityInfo.parse(ckass).getTableName());
    }

    static Model fromAs(Class<?> ckass, String asName)
    {
        BaseModel model = new BaseModel();
        model.addFromAs(new BaseModel.FromAs(ckass, asName));
        return model;
    }

    <T> Model select(SFunction<T, ?>... fns);

    Model selectAll(Class<?> ckass);

    <T> Model selectAs(SFunction<T, ?> fn, String asName);

    <T> Model selectCount(SFunction<T, ?> fn);

    Model selectCount();

    <T> Model set(SFunction<T, ?> fn, Object value);

    <T> Model insert(SFunction<T, ?> fn, Object value);

    Model leftJoin(Class ckass);

    Model rightJoin(Class<?> ckass);

    Model fullJoin(Class<?> ckass);

    Model innerJoin(Class<?> ckass);

    <E, T> Model on(SFunction<T, ?> fn1, SFunction<E, ?> fn2);

    Model where(Param param);

    <T> Model orderBy(SFunction<T, ?> fn, boolean desc);

    <T> Model groupBy(SFunction<T, ?> fn);

    Model returnType(Class<?> ckass);

    Model page(Page page);

    BaseModel.ModelResult getResult();
}
