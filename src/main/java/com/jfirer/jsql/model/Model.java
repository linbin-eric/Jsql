package com.jfirer.jsql.model;

import com.jfirer.jsql.metadata.Page;
import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.model.support.LockMode;
import com.jfirer.jsql.model.support.SFunction;

public interface Model
{
    default Model from(Class<?> ckass)
    {
        return fromAs(ckass, TableEntityInfo.parse(ckass).getTableName());
    }

    Model fromAs(Class<?> ckass, String asName);

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

    Model lockMode(LockMode lockMode);

    Model page(Page page);

    BaseModel.ModelResult getResult();
}
