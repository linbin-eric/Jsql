package com.jfireframework.sql.dao.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.SessionfactoryConfig;
import com.jfireframework.sql.dao.StrategyOperation;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.resultsettransfer.impl.BeanTransfer;
import com.jfireframework.sql.session.ExecSqlTemplate;

public class StrategyOperationImpl implements StrategyOperation
{
    class FindStrategySql
    {
        String       sql;
        BeanTransfer transfer;
    }
    
    private final Class<?>                               ckass;
    private final Map<String, MapField>                  mapFields;
    private final ConcurrentMap<String, FindStrategySql> findMap   = new ConcurrentHashMap<String, StrategyOperationImpl.FindStrategySql>();
    private final ConcurrentMap<String, String>          updateMap = new ConcurrentHashMap<String, String>();
    private final ConcurrentMap<String, String>          deleteMap = new ConcurrentHashMap<String, String>();
    private final ConcurrentMap<String, String>          countMap  = new ConcurrentHashMap<String, String>();
    private final String                                 tableName;
    private SqlInterceptor[]                             sqlInterceptors;
    private SessionfactoryConfig                         config;
    private PageParse                                    pageParse;
    
    public StrategyOperationImpl(Class<?> ckass, MapField[] mapFields, SessionfactoryConfig config, SqlInterceptor[] sqlInterceptors, String tableName, PageParse pageParse)
    {
        this.ckass = ckass;
        this.config = config;
        this.mapFields = parse(mapFields);
        this.tableName = tableName;
        this.sqlInterceptors = sqlInterceptors;
        this.pageParse = pageParse;
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
    
    private String getUpdate(String strategy)
    {
        String updateStrategySql = updateMap.get(strategy);
        if (updateStrategySql == null)
        {
            updateStrategySql = buildUpdate(strategy);
            updateMap.putIfAbsent(strategy, updateStrategySql);
        }
        return updateStrategySql;
    }
    
    private String getDelete(String strategy)
    {
        String delete = deleteMap.get(strategy);
        if (delete == null)
        {
            delete = buildDelete(strategy);
            deleteMap.putIfAbsent(strategy, delete);
        }
        return delete;
    }
    
    private String buildDelete(String strategy)
    {
        StringCache cache = new StringCache();
        cache.append("delete from ").append(tableName).append(" where ");
        for (String field : strategy.split(","))
        {
            Verify.notNull(mapFields.get(field), "策略:{}中的字段:{}不存在", strategy, field);
            cache.append(mapFields.get(field).getColName()).append("=? and ");
        }
        return cache.deleteEnds(4).toString();
    }
    
    private FindStrategySql buildFind(String fields)
    {
        StringCache cache = new StringCache();
        cache.append("select ");
        String[] tmp = fields.split(";");
        String valueFields = tmp[0];
        String whereFields = tmp.length > 1 ? tmp[1] : null;
        String orderFields = tmp.length > 2 ? tmp[2] : null;
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
                if (StringUtil.isNotBlank(selectField) == false)
                {
                    continue;
                }
                Verify.notNull(mapFields.get(selectField), "策略:{}中的字段:{}不存在", fields, selectField);
                cache.append(mapFields.get(selectField).getColName()).appendComma();
            }
            if (StringUtil.isNotBlank(whereFields))
            {
                for (String whereField : whereFields.split(","))
                {
                    Verify.notNull(mapFields.get(whereField), "策略:{}中的字段:{}不存在", fields, whereField);
                    cache.append(mapFields.get(whereField).getColName()).appendComma();
                }
            }
        }
        cache.deleteLast().append(" from ").append(tableName);
        if (StringUtil.isNotBlank(whereFields))
        {
            cache.append(" where ");
            for (String whereField : whereFields.split(","))
            {
                Verify.notNull(mapFields.get(whereField), "策略:{}中的字段:{}不存在", fields, whereField);
                cache.append(mapFields.get(whereField).getColName()).append("=? and ");
            }
            cache.deleteEnds(4);
        }
        if (StringUtil.isNotBlank(orderFields))
        {
            cache.append(" order by ");
            for (String each : orderFields.split(","))
            {
                if (each.contains(":"))
                {
                    String[] orderRule = each.split(":");
                    Verify.notNull(mapFields.get(orderRule[0]).getColName(), "策略:{}中的字段:{}不存在", fields, orderRule[0]);
                    cache.append(mapFields.get(orderRule[0]).getColName()).append(" ").append(orderRule[1]).append(",");
                    if ("aes".equals(orderRule[1]) || "desc".equals(orderRule[1]))
                    {
                        ;
                    }
                    else
                    {
                        throw new UnsupportedOperationException(StringUtil.format("策略:{}中排序内容:{} 错误", fields, orderRule[1]));
                    }
                }
                else
                {
                    Verify.notNull(mapFields.get(each), "策略:{}中的字段:{}不存在", fields, each);
                    cache.append(mapFields.get(each)).append(",");
                }
            }
            cache.deleteLast();
        }
        FindStrategySql findStrategySql = new FindStrategySql();
        findStrategySql.sql = cache.toString();
        findStrategySql.transfer = new BeanTransfer();
        findStrategySql.transfer.initialize(ckass, config);
        return findStrategySql;
    }
    
    private String buildUpdate(String fields)
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
        return cache.deleteEnds(4).toString();
    }
    
    @Override
    public int update(Connection connection, String strategy, Object... params)
    {
        String strategySql = getUpdate(strategy);
        return ExecSqlTemplate.update(sqlInterceptors, connection, strategySql, params);
    }
    
    @Override
    public <T> T findOne(Connection connection, String strategy, Object... params)
    {
        FindStrategySql strategySql = getFind(strategy);
        return ExecSqlTemplate.queryOne(sqlInterceptors, strategySql.transfer, connection, strategySql.sql, params);
    }
    
    @Override
    public <T> List<T> findAll(Connection connection, String strategy, Object... params)
    {
        FindStrategySql strategySql = getFind(strategy);
        return ExecSqlTemplate.queryList(sqlInterceptors, strategySql.transfer, connection, strategySql.sql, params);
    }
    
    @Override
    public <T> List<T> findPage(Connection connection, Page page, String strategy, Object... params)
    {
        FindStrategySql strategySql = getFind(strategy);
        return ExecSqlTemplate.pageQuery(sqlInterceptors, pageParse, page, strategySql.transfer, connection, strategySql.sql, params);
    }
    
    @Override
    public int delete(Connection connection, String strategy, Object... params)
    {
        String delete = getDelete(strategy);
        return ExecSqlTemplate.update(sqlInterceptors, connection, delete, params);
    }
    
    @Override
    public int count(Connection connection, String strategy, Object... params)
    {
        String count = getCount(strategy);
        return ExecSqlTemplate.count(sqlInterceptors, connection, count, params);
    }
    
    private String getCount(String strategy)
    {
        String sql = countMap.get(strategy);
        if (sql == null)
        {
            sql = buildCount(strategy);
            countMap.putIfAbsent(strategy, sql);
        }
        return sql;
    }
    
    private String buildCount(String strategy)
    {
        StringCache cache = new StringCache();
        cache.append("select count(*) from ").append(tableName);
        if ("".equals(strategy))
        {
            return cache.toString();
        }
        else
        {
            cache.append(" where ");
            for (String whereField : strategy.split(","))
            {
                Verify.notNull(mapFields.get(whereField), "策略:{}中的字段:{}不存在", strategy, whereField);
                cache.append(mapFields.get(whereField).getColName()).append("=? and ");
            }
            return cache.deleteEnds(4).toString();
        }
    }
}
