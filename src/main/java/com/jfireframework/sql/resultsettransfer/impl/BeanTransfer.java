package com.jfireframework.sql.resultsettransfer.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.annotation.NameStrategy;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultNameStrategy;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.resultsettransfer.field.MapFieldFactory;

public class BeanTransfer<T> extends AbstractResultsetTransfer<T>
{
    protected final Map<String, MapField[]>             mapFields;
    protected final Class<T>                            type;
    private final ConcurrentHashMap<String, MapField[]> fieldCache = new ConcurrentHashMap<String, MapField[]>();
    
    public BeanTransfer(Class<T> type)
    {
        super(type);
        this.type = type;
        ColNameStrategy colNameStrategy;
        try
        {
            Class<? extends ColNameStrategy> ckass = type.isAnnotationPresent(NameStrategy.class) ? type.getAnnotation(NameStrategy.class).value() : DefaultNameStrategy.class;
            colNameStrategy = ckass.newInstance();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        List<MapField> list = new ArrayList<MapField>();
        for (Field each : ReflectUtil.getAllFields(type))
        {
            if (each.isAnnotationPresent(SqlIgnore.class) || Map.class.isAssignableFrom(each.getType()) || List.class.isAssignableFrom(each.getType()) || each.getType().isInterface() || Modifier.isStatic(each.getModifiers()))
            {
                continue;
            }
            list.add(MapFieldFactory.buildMapField(each, colNameStrategy));
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
    protected T valueOf(ResultSet resultSet, String sql) throws Exception
    {
        MapField[] fields = fieldCache.get(sql);
        if (fields == null)
        {
            fields = buildFieldsFromMetadata(resultSet.getMetaData());
            fieldCache.put(sql, fields);
        }
        T entity = type.newInstance();
        for (MapField each : fields)
        {
            each.setEntityValue(entity, resultSet);
        }
        return entity;
    }
    
    private MapField[] buildFieldsFromMetadata(ResultSetMetaData metaData) throws SQLException
    {
        int colCount = metaData.getColumnCount();
        List<MapField> resultFields = new LinkedList<MapField>();
        for (int i = 0; i < colCount; i++)
        {
            MapField[] fit = mapFields.get(metaData.getColumnName(i + 1).toLowerCase());
            if (fit != null)
            {
                for (MapField each : fit)
                {
                    resultFields.add(each);
                }
            }
        }
        MapField[] fields = resultFields.toArray(new MapField[resultFields.size()]);
        return fields;
    }
}
