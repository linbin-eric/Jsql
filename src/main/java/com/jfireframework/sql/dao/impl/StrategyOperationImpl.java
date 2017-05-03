package com.jfireframework.sql.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dao.StrategyOperation;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.FixBeanTransfer;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.session.SqlSession;

public class StrategyOperationImpl<T> implements StrategyOperation<T>
{
    class FindStrategySql
    {
        String             sql;
        FixBeanTransfer<T> transfer;
    }
    
    class UpdateStrategySql
    {
        String sql;
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
    
    private Map<String, MapField> parse(MapField[] mapFields)
    {
        Map<String, MapField> map = new HashMap<String, MapField>();
        for (MapField each : mapFields)
        {
            map.put(each.getFieldName(), each);
        }
        return map;
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
    
    private FindStrategySql buildFind(String fields)
    {
        StringCache cache = new StringCache();
        cache.append("select ");
        String[] tmp = fields.split(";");
        String valueFields = tmp[0];
        String whereFields = tmp[1];
        if (valueFields.equals("*"))
        {
            for (MapField each : mapFields.values())
            {
                cache.append(each.getColName()).appendComma();
            }
        }
        else
        {
            for (String selectField : valueFields.split(","))
            {
                Verify.notNull(mapFields.get(selectField), "策略:{}中的字段:{}不存在", fields, selectField);
                cache.append(mapFields.get(selectField).getColName()).appendComma();
            }
            for (String whereField : whereFields.split(","))
            {
                Verify.notNull(mapFields.get(whereField), "策略:{}中的字段:{}不存在", fields, whereField);
                cache.append(mapFields.get(whereField).getColName()).appendComma();
            }
        }
        cache.deleteLast().append(" from ").append(tableName).append(" where ");
        for (String whereField : whereFields.split(","))
        {
            Verify.notNull(mapFields.get(whereField), "策略:{}中的字段:{}不存在", fields, whereField);
            cache.append(mapFields.get(whereField).getColName()).append("=? and ");
        }
        cache.deleteEnds(4);
        FindStrategySql findStrategySql = new FindStrategySql();
        findStrategySql.sql = cache.toString();
        findStrategySql.transfer = new FixBeanTransfer<T>(ckass);
        return findStrategySql;
    }
    
    private UpdateStrategySql buildUpdate(String fields)
    {
        StringCache cache = new StringCache();
        cache.append("update ").append(tableName).append(" set ");
        String[] tmp = fields.split(";");
        String valueFields = tmp[0];
        String whereFields = tmp[1];
        for (String setField : valueFields.split(","))
        {
            Verify.notNull(mapFields.get(setField), "策略:{}中的字段:{}不存在", fields, setField);
            cache.append(mapFields.get(setField).getColName()).append("=?,");
        }
        cache.deleteLast().append(" where ");
        for (String whereField : whereFields.split(","))
        {
            Verify.notNull(mapFields.get(whereField), "策略:{}中的字段:{}不存在", fields, whereField);
            cache.append(mapFields.get(whereField).getColName()).append("=? and ");
        }
        cache.deleteEnds(4);
        UpdateStrategySql updateStrategySql = new UpdateStrategySql();
        updateStrategySql.sql = cache.toString();
        return updateStrategySql;
    }
    
    @Override
    public int update(SqlSession session, String strategy, Object... params)
    {
        UpdateStrategySql strategySql = getUpdate(strategy);
        return session.update(strategySql.sql, params);
    }
    
    @Override
    public T findOne(SqlSession session, String strategy, Object... params)
    {
        FindStrategySql strategySql = getFind(strategy);
        return session.query(strategySql.transfer, strategySql.sql, params);
    }
    
    @Override
    public List<T> findAll(SqlSession session, String strategy, Object... params)
    {
        FindStrategySql strategySql = getFind(strategy);
        return session.queryList(strategySql.transfer, strategySql.sql, params);
    }
    
    @Override
    public List<T> findPage(SqlSession session, Page page, String strategy, Object... params)
    {
        FindStrategySql strategySql = getFind(strategy);
        return session.queryList(strategySql.transfer, strategy, page, params);
    }
    
}
