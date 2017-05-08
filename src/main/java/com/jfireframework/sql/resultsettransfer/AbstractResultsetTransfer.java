package com.jfireframework.sql.resultsettransfer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.NameStrategy;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultNameStrategy;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.resultsettransfer.field.MapFieldBuilder;

public abstract class AbstractResultsetTransfer<T> implements ResultSetTransfer<T>
{
    protected Map<String, MapField[]> mapFields;
    protected Class<T>                entityClass;
    
    public AbstractResultsetTransfer()
    {
    }
    
    public AbstractResultsetTransfer(Class<T> entityClass)
    {
        this.entityClass = entityClass;
        ColNameStrategy colNameStrategy;
        try
        {
            Class<? extends ColNameStrategy> ckass = entityClass.isAnnotationPresent(NameStrategy.class) ? entityClass.getAnnotation(NameStrategy.class).value() : DefaultNameStrategy.class;
            colNameStrategy = ckass.newInstance();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        List<MapField> list = new ArrayList<MapField>();
        for (Field each : ReflectUtil.getAllFields(entityClass))
        {
            if (each.isAnnotationPresent(SqlIgnore.class) || Map.class.isAssignableFrom(each.getType()) || List.class.isAssignableFrom(each.getType()) || each.getType().isInterface() || Modifier.isStatic(each.getModifiers()))
            {
                continue;
            }
            list.add(MapFieldBuilder.buildMapField(each, colNameStrategy));
        }
        mapFields = new HashMap<String, MapField[]>();
        for (MapField each : list)
        {
            if (mapFields.containsKey(each.getColName().toLowerCase()) == false)
            {
                mapFields.put(each.getColName().toLowerCase(), new MapField[] { each });
            }
            else
            {
                MapField[] exists = mapFields.get(each.getColName().toLowerCase());
                MapField[] newPut = new MapField[exists.length + 1];
                System.arraycopy(exists, 0, newPut, 0, exists.length);
                newPut[exists.length] = each;
                mapFields.put(each.getColName().toLowerCase(), newPut);
            }
        }
    }
    
    @Override
    public T transfer(ResultSet resultSet, String sql) throws Exception
    {
        if (resultSet.next())
        {
            T result = valueOf(resultSet, sql);
            if (resultSet.next())
            {
                throw new IllegalArgumentException(StringUtil.format("存在2行数据，不符合返回值要求。"));
            }
            else
            {
                return result;
            }
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public List<T> transferList(ResultSet resultSet, String sql) throws Exception
    {
        List<T> list = new LinkedList<T>();
        while (resultSet.next())
        {
            list.add(valueOf(resultSet, sql));
        }
        return list;
    }
    
    protected abstract T valueOf(ResultSet resultSet, String sql) throws Exception;
}
