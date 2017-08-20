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
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.annotation.NameStrategy;
import com.jfireframework.sql.annotation.SqlIgnore;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.dbstructure.name.DefaultNameStrategy;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.mapfield.impl.MapFieldImpl;

public class BeanTransfer extends AbstractResultsetTransfer
{
    protected Map<String, MapField>                     mapFields;
    protected Class<?>                                  type;
    private final ConcurrentHashMap<String, MapField[]> fieldCache = new ConcurrentHashMap<String, MapField[]>();
    
    @Override
    protected Object valueOf(ResultSet resultSet, String sql) throws Exception
    {
        MapField[] fields = fieldCache.get(sql);
        if (fields == null)
        {
            fields = buildFieldsFromMetadata(resultSet.getMetaData());
            fieldCache.put(sql, fields);
        }
        Object entity = type.newInstance();
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
            MapField fit = mapFields.get(metaData.getColumnName(i + 1).toLowerCase());
            if (fit != null)
            {
                resultFields.add(fit);
            }
        }
        MapField[] fields = resultFields.toArray(new MapField[resultFields.size()]);
        return fields;
    }
    
    @Override
    public void initialize(Class<?> type, SessionfactoryConfig config)
    {
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
            list.add(new MapFieldImpl(each, colNameStrategy, config.getFieldOperatorDictionary()));
        }
        mapFields = new HashMap<String, MapField>();
        for (MapField each : list)
        {
            mapFields.put(each.getColName().toLowerCase(), each);
        }
    }
}
