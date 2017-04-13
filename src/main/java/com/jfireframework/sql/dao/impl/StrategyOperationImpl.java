package com.jfireframework.sql.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dao.StrategyOperation;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.resultsettransfer.FixBeanTransfer;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.session.SqlSession;

public class StrategyOperationImpl<T> implements StrategyOperation<T>
{
    class FindStrategySql
    {
        String             sql;
        FixBeanTransfer<T> transfer;
        MapField[]         selectFields;
        MapField[]         whereFields;
    }
    
    class UpdateStrategySql
    {
        String             sql;
        FixBeanTransfer<T> transfer;
        MapField[]         fields;
    }
    
    private final Class<T>                                 ckass;
    private final Map<String, MapField>                    mapFields;
    private final ConcurrentMap<String, FindStrategySql>   findMap   = new ConcurrentHashMap<String, StrategyOperationImpl<T>.FindStrategySql>();
    private final ConcurrentMap<String, UpdateStrategySql> updateMap = new ConcurrentHashMap<String, StrategyOperationImpl<T>.UpdateStrategySql>();
    private final String                                   tableName;
    
    public StrategyOperationImpl(Class<T> ckass, MapField[] mapFields)
    {
        this.ckass = ckass;
        this.mapFields = parse(mapFields);
        tableName = ckass.getAnnotation(TableEntity.class).name();
    }
    
    private FindStrategySql getFind(String strategy)
    {
        FindStrategySql findStrategySql = findMap.get(strategy);
        if (findStrategySql == null)
        {
            findStrategySql = buildFind(strategy);
            findMap.putIfAbsent(strategy, findStrategySql);
        }
        return findStrategySql;
    }
    
    private UpdateStrategySql getUpdate(String strategy)
    {
        UpdateStrategySql updateStrategySql = updateMap.get(strategy);
        if (updateStrategySql == null)
        {
            updateStrategySql = buildUpdate(strategy);
            updateMap.putIfAbsent(strategy, updateStrategySql);
        }
        return updateStrategySql;
    }
    
    private Map<String, MapField> parse(MapField[] mapFields)
    {
        Map<String, MapField> map = new HashMap<String, MapField>();
        for (MapField each : mapFields)
        {
            map.put(each.getFieldName(), each);
        }
        return map;
    }
    
    private FindStrategySql buildFind(String fields)
    {
        StringCache cache = new StringCache();
        List<MapField> selectFields = new LinkedList<MapField>();
        List<MapField> whereFields = new LinkedList<MapField>();
        cache.append("select ");
        String[] tmp = fields.split(";");
        String t_valueFields = tmp[0];
        String t_whereFields = tmp[1];
        if (t_valueFields.equals("*"))
        {
            for (MapField each : mapFields.values())
            {
                cache.append(each.getColName()).appendComma();
                selectFields.add(each);
            }
        }
        else
        {
            for (String selectField : t_valueFields.split(","))
            {
                cache.append(mapFields.get(selectField).getColName()).appendComma();
                selectFields.add(mapFields.get(selectField));
            }
            for (String whereField : t_whereFields.split(","))
            {
                cache.append(mapFields.get(whereField).getColName()).appendComma();
                selectFields.add(mapFields.get(whereField));
            }
        }
        cache.deleteLast().append(" from ").append(tableName).append(" where ");
        for (String whereField : t_whereFields.split(","))
        {
            cache.append(mapFields.get(whereField).getColName()).append("=? and ");
            whereFields.add(mapFields.get(whereField));
        }
        cache.deleteEnds(4);
        FindStrategySql findStrategySql = new FindStrategySql();
        findStrategySql.sql = cache.toString();
        findStrategySql.selectFields = selectFields.toArray(new MapField[selectFields.size()]);
        findStrategySql.whereFields = whereFields.toArray(new MapField[whereFields.size()]);
        findStrategySql.transfer = new FixBeanTransfer<T>(ckass);
        return findStrategySql;
    }
    
    private UpdateStrategySql buildUpdate(String fields)
    {
        StringCache cache = new StringCache();
        List<MapField> list = new LinkedList<MapField>();
        cache.append("update ").append(tableName).append(" set ");
        String[] tmp = fields.split(";");
        String t_valueFields = tmp[0];
        String t_whereFields = tmp[1];
        for (String setField : t_valueFields.split(","))
        {
            cache.append(mapFields.get(setField).getColName()).append("=?,");
            list.add(mapFields.get(setField));
        }
        cache.deleteLast().append(" where ");
        for (String whereField : t_whereFields.split(","))
        {
            cache.append(mapFields.get(whereField).getColName()).append("=? and ");
            list.add(mapFields.get(whereField));
        }
        cache.deleteEnds(4);
        UpdateStrategySql updateStrategySql = new UpdateStrategySql();
        updateStrategySql.sql = cache.toString();
        updateStrategySql.fields = list.toArray(new MapField[list.size()]);
        updateStrategySql.transfer = new FixBeanTransfer<T>(ckass);
        return updateStrategySql;
    }
    
    @Override
    public T findOne(SqlSession session, T param, String strategy)
    {
        FindStrategySql findStrategySql = getFind(strategy);
        String sql = findStrategySql.sql;
        return session.query(findStrategySql.transfer, sql, parseParams(findStrategySql.whereFields, param));
    }
    
    private Object[] parseParams(MapField[] fields, Object entity)
    {
        Object[] params = new Object[fields.length];
        for (int i = 0; i < fields.length; i++)
        {
            params[i] = fields[i].statementValue(entity);
        }
        return params;
    }
    
    @Override
    public List<T> findAll(SqlSession session, T param, String strategy)
    {
        FindStrategySql findStrategySql = getFind(strategy);
        return session.queryList(findStrategySql.transfer, findStrategySql.sql, parseParams(findStrategySql.whereFields, param));
    }
    
    @Override
    public List<T> findPage(SqlSession session, T param, Page page, PageParse pageParse, String strategy)
    {
        FindStrategySql findStrategySql = getFind(strategy);
        return session.queryList(findStrategySql.transfer, strategy, page, parseParams(findStrategySql.whereFields, param));
    }
    
    @Override
    public int update(SqlSession session, T param, String strategy)
    {
        UpdateStrategySql updateStrategySql = getUpdate(strategy);
        String sql = updateStrategySql.sql;
        return session.update(sql, parseParams(updateStrategySql.fields, param));
    }
    
}
