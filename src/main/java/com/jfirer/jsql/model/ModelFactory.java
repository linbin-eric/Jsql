package com.jfirer.jsql.model;

import com.jfirer.jsql.model.support.SFunction;

public class ModelFactory
{
    public static Model update(Class<?> ckass)
    {
        return new BaseModel(new BaseModel.Update(ckass));
    }

    public static Model insert(Class<?> ckass)
    {
        return new BaseModel(new BaseModel.InsertInto(ckass));
    }

    public static <T> Model insert(T entity)
    {
        return new BaseModel(new BaseModel.InsertIntoWithObject(entity));
    }

    public static Model deleteFrom(Class<?> ckass)
    {
        return new BaseModel(new BaseModel.Delete(ckass));
    }

    public static <T> Model save(T entity)
    {
        return new BaseModel(new BaseModel.SaveWithObject(entity));
    }

    public static <T> Model update(T entity)
    {
        return new BaseModel(new BaseModel.UpdateWithObject(entity));
    }

    public static <T> Model select(SFunction<T, ?>... fns)
    {
        BaseModel model = new BaseModel();
        model.select(fns);
        return model;
    }

    public static Model selectAll()
    {
        return new BaseModel();
    }

    public static <T> Model selectAs(SFunction<T, ?> fn, String asName)
    {
        BaseModel model = new BaseModel();
        model.selectAs(fn, asName);
        return model;
    }

    public static <T> Model selectCount(SFunction<T, ?> fn)
    {
        BaseModel model = new BaseModel();
        model.selectCount(fn);
        return model;
    }

    public static Model selectCount()
    {
        BaseModel model = new BaseModel();
        model.selectCount();
        return model;
    }
}
