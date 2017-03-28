package com.jfireframework.sql.dao.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.simplelog.ConsoleLogFactory;
import com.jfireframework.baseutil.simplelog.Logger;
import com.jfireframework.baseutil.uniqueid.Uid;
import com.jfireframework.sql.annotation.FindBy;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.dao.StrategyOperation;
import com.jfireframework.sql.dbstructure.ColNameStrategy;
import com.jfireframework.sql.interceptor.SqlPreInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.metadata.TableMetaData.FieldInfo;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.page.PageParse;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.resultsettransfer.field.MapFieldBuilder;
import sun.misc.Unsafe;

public abstract class BaseDAO<T> implements Dao<T>
{
    static class SqlAndFields
    {
        protected final String     sql;
        protected final MapField[] fields;
        
        public SqlAndFields(String sql, MapField[] fields)
        {
            this.sql = sql;
            this.fields = fields;
        }
        
        public String getSql()
        {
            return sql;
        }
        
        public MapField[] getFields()
        {
            return fields;
        }
        
    }
    
    protected final Class<T>             entityClass;
    // 代表数据库主键id的field
    protected final MapField             idField;
    protected final long                 idOffset;
    protected final IdType               idType;
    protected final static Unsafe        unsafe    = ReflectUtil.getUnsafe();
    protected final String               tableName;
    protected final SqlAndFields         getInfo;
    protected final SqlAndFields         getInShareInfo;
    protected final SqlAndFields         getForUpdateInfo;
    protected final SqlAndFields         updateInfo;
    protected final String               deleteSql;
    protected static final Logger        LOGGER    = ConsoleLogFactory.getLogger();
    protected final SqlPreInterceptor[]  preInterceptors;
    protected final Uid                  uid;
    protected final boolean              useUid;
    protected final StrategyOperation<T> strategyOperation;
    protected final Map<String, String>  findByMap = new HashMap<String, String>();
    
    enum IdType
    {
        INT, LONG, STRING
    }
    
    @SuppressWarnings("unchecked")
    public BaseDAO(TableMetaData metaData, SqlPreInterceptor[] preInterceptors, Uid uid)
    {
        this.uid = uid;
        this.preInterceptors = preInterceptors;
        this.entityClass = (Class<T>) metaData.getEntityClass();
        ColNameStrategy nameStrategy = metaData.getColNameStrategy();
        tableName = entityClass.getAnnotation(TableEntity.class).name();
        MapField[] allMapFields = buildMapfields(metaData.getFieldInfos(), nameStrategy);
        MapField t_id = null;
        for (MapField mapField : allMapFields)
        {
            if (mapField.getField().isAnnotationPresent(Id.class))
            {
                t_id = mapField;
            }
            if (mapField.getField().isAnnotationPresent(FindBy.class))
            {
                String sql = "select * from " + tableName + " where " + mapField.getColName() + " = ?";
                findByMap.put(mapField.getFieldName(), sql);
            }
        }
        Field t_idField = t_id.getField();
        useUid = t_idField.getAnnotation(Id.class).useUid();
        idType = getIdType(t_idField);
        idField = t_id;
        idOffset = unsafe.objectFieldOffset(t_idField);
        updateInfo = buildUpdate(allMapFields, idField);
        getInfo = buildGet(allMapFields, idField);
        getForUpdateInfo = buildGetForUpdate(allMapFields, idField);
        getInShareInfo = buildGetInShare(allMapFields, idField);
        useForSelf(allMapFields, idField);
        deleteSql = "delete from " + tableName + " where " + idField.getColName() + "=?";
        logSql();
        strategyOperation = new StrategyOperationImpl<T>(entityClass, allMapFields, preInterceptors);
    }
    
    protected abstract void useForSelf(MapField[] fields, MapField idField);
    
    protected void logSql()
    {
        LOGGER.debug("为表{},类{}创建的更新语句是{}", tableName, entityClass.getName(), updateInfo.getSql());
        LOGGER.debug("为表{},类{}创建的获取语句是{}", tableName, entityClass.getName(), getInfo.getSql());
        LOGGER.debug("为表{},类{}创建的获取加锁语句是{}", tableName, entityClass.getName(), getForUpdateInfo.getSql());
        LOGGER.debug("为表{},类{}创建的获取共享语句是{}", tableName, entityClass.getName(), getInShareInfo.getSql());
        LOGGER.debug("为表{},类{}创建的删除语句是{}", tableName, entityClass.getName(), deleteSql);
    }
    
    protected SqlAndFields buildGet(MapField[] fields, MapField idField)
    {
        List<MapField> getFields = new LinkedList<MapField>();
        StringCache cache = new StringCache();
        /******** 生成getSql ******/
        cache.clear();
        cache.append("select ");
        for (MapField each : fields)
        {
            if (each.loadIgnore())
            {
                continue;
            }
            getFields.add(each);
            cache.append(each.getColName()).append(",");
        }
        cache.deleteLast().append(" from ").append(tableName).append(" where ").append(idField.getColName()).append("=?");
        return new SqlAndFields(cache.toString(), getFields.toArray(new MapField[getFields.size()]));
    }
    
    protected SqlAndFields buildUpdate(MapField[] fields, MapField idField)
    {
        List<MapField> updateFields = new LinkedList<MapField>();
        StringCache cache = new StringCache();
        cache.append("update ").append(tableName).append(" set ");
        for (MapField each : fields)
        {
            if (each == idField || each.saveIgnore())
            {
                continue;
            }
            updateFields.add(each);
            cache.append(each.getColName()).append("=?,");
        }
        cache.deleteLast().append(" where ").append(idField.getColName()).append("=?");
        updateFields.add(idField);
        return new SqlAndFields(cache.toString(), updateFields.toArray(new MapField[updateFields.size()]));
    }
    
    protected SqlAndFields buildGetForUpdate(MapField[] fields, MapField idField)
    {
        StringCache cache = new StringCache();
        /******** 生成getSql ******/
        cache.clear();
        cache.append("select ");
        List<MapField> getForUpdateFields = new LinkedList<MapField>();
        for (MapField each : fields)
        {
            if (each.loadIgnore())
            {
                continue;
            }
            getForUpdateFields.add(each);
            cache.append(each.getColName()).append(",");
        }
        cache.deleteLast().append(" from ").append(tableName).append(" where ").append(idField.getColName()).append("=? for update");
        return new SqlAndFields(cache.toString(), getForUpdateFields.toArray(new MapField[getForUpdateFields.size()]));
    }
    
    protected SqlAndFields buildGetInShare(MapField[] fields, MapField idField)
    {
        StringCache cache = new StringCache();
        /******** 生成getSql ******/
        cache.clear();
        cache.append("select ");
        List<MapField> getInSahreFields = new LinkedList<MapField>();
        for (MapField each : fields)
        {
            if (each.loadIgnore())
            {
                continue;
            }
            getInSahreFields.add(each);
            cache.append(each.getColName()).append(",");
        }
        cache.deleteLast().append(" from ").append(tableName).append(" where ").append(idField.getColName()).append("=? lock in share mode");
        return new SqlAndFields(cache.toString(), getInSahreFields.toArray(new MapField[getInSahreFields.size()]));
    }
    
    protected IdType getIdType(Field field)
    {
        Class<?> type = field.getType();
        if (type == String.class)
        {
            return IdType.STRING;
        }
        else if (type == Integer.class)
        {
            return IdType.INT;
        }
        else if (type == Long.class)
        {
            return IdType.LONG;
        }
        else
        {
            throw new UnsupportedOperationException("id字段只支持Integer，Long，String");
        }
        
    }
    
    protected MapField[] buildMapfields(FieldInfo[] infos, ColNameStrategy colNameStrategy)
    {
        List<MapField> list = new ArrayList<MapField>(infos.length);
        for (FieldInfo each : infos)
        {
            list.add(MapFieldBuilder.buildMapField(each.getField(), colNameStrategy));
        }
        return list.toArray(new MapField[list.size()]);
    }
    
    @Override
    public int delete(Object entity, Connection connection)
    {
        PreparedStatement pstat = null;
        try
        {
            pstat = connection.prepareStatement(deleteSql);
            switch (idType)
            {
                case INT:
                    pstat.setInt(1, (Integer) unsafe.getObject(entity, idOffset));
                    break;
                case LONG:
                    pstat.setLong(1, (Long) unsafe.getObject(entity, idOffset));
                    break;
                case STRING:
                    pstat.setString(1, (String) unsafe.getObject(entity, idOffset));
                    break;
            }
            return pstat.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (pstat != null)
            {
                try
                {
                    pstat.close();
                }
                catch (SQLException e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T getById(Object pk, Connection connection)
    {
        for (SqlPreInterceptor each : preInterceptors)
        {
            each.preIntercept(getInfo.getSql(), pk);
        }
        PreparedStatement pStat = null;
        try
        {
            pStat = connection.prepareStatement(getInfo.getSql());
            switch (idType)
            {
                case INT:
                    pStat.setInt(1, (Integer) pk);
                    break;
                case LONG:
                    pStat.setLong(1, (Long) pk);
                    break;
                case STRING:
                    pStat.setString(1, (String) pk);
                    break;
            }
            ResultSet resultSet = pStat.executeQuery();
            if (resultSet.next())
            {
                Object entity = entityClass.newInstance();
                for (MapField each : getInfo.getFields())
                {
                    each.setEntityValue(entity, resultSet);
                }
                return (T) entity;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            if (pStat != null)
            {
                try
                {
                    pStat.close();
                }
                catch (SQLException e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
    }
    
    @Override
    public void save(T entity, Connection connection)
    {
        Object idValue = unsafe.getObject(entity, idOffset);
        if (idValue == null)
        {
            insert(entity, null, connection);
        }
        else
        {
            update(entity, connection);
        }
        
    }
    
    @Override
    public int update(T entity, Connection connection)
    {
        for (SqlPreInterceptor each : preInterceptors)
        {
            each.preIntercept(updateInfo.getSql(), entity);
        }
        PreparedStatement pStat = null;
        try
        {
            pStat = connection.prepareStatement(updateInfo.getSql());
            int index = 1;
            for (MapField each : updateInfo.getFields())
            {
                each.setStatementValue(pStat, entity, index);
                index++;
            }
            return pStat.executeUpdate();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            if (pStat != null)
            {
                try
                {
                    pStat.close();
                }
                catch (SQLException e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
    }
    
    protected abstract void insert(T entity, Object idValue, Connection connection);
    
    @Override
    public void insert(T entity, Connection connection)
    {
        Object idValue = unsafe.getObject(entity, idOffset);
        insert(entity, idValue, connection);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T getById(Object pk, Connection connection, LockMode mode)
    {
        String sql = mode == LockMode.SHARE ? getInShareInfo.getSql() : getForUpdateInfo.getSql();
        for (SqlPreInterceptor each : preInterceptors)
        {
            each.preIntercept(sql, pk);
        }
        PreparedStatement pStat = null;
        try
        {
            pStat = connection.prepareStatement(sql);
            pStat.setObject(1, pk);
            ResultSet resultSet = pStat.executeQuery();
            if (resultSet.next())
            {
                Object entity = entityClass.newInstance();
                for (MapField each : getInfo.getFields())
                {
                    each.setEntityValue(entity, resultSet);
                }
                return (T) entity;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            if (pStat != null)
            {
                try
                {
                    pStat.close();
                }
                catch (SQLException e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
    }
    
    @Override
    public T findBy(Connection connection, Object param, String name)
    {
        String sql = findByMap.get(name);
        if (sql == null)
        {
            throw new NullPointerException("没有属性:" + name + "的findBy注解,请检查类:" + entityClass.getName());
        }
        for (SqlPreInterceptor each : preInterceptors)
        {
            each.preIntercept(sql, param);
        }
        PreparedStatement pStat = null;
        try
        {
            pStat = connection.prepareStatement(sql);
            pStat.setObject(1, param);
            ResultSet resultSet = pStat.executeQuery();
            if (resultSet.next())
            {
                T entity = entityClass.newInstance();
                for (MapField each : getInfo.getFields())
                {
                    each.setEntityValue(entity, resultSet);
                }
                if (resultSet.next())
                {
                    throw new IllegalArgumentException("查询存在两个或以上的数据，不符合要求");
                }
                return entity;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            if (pStat != null)
            {
                try
                {
                    pStat.close();
                }
                catch (SQLException e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
        
    }
    
    @Override
    public int deleteAll(Connection connection)
    {
        PreparedStatement preparedStatement = null;
        try
        {
            preparedStatement = connection.prepareStatement("delete from " + tableName);
            return preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            if (preparedStatement != null)
            {
                try
                {
                    preparedStatement.close();
                }
                catch (SQLException e)
                {
                    throw new JustThrowException(e);
                }
            }
        }
    }
    
    @Override
    public int update(Connection connection, T param, String strategyName)
    {
        return strategyOperation.update(connection, param, strategyName);
    }
    
    @Override
    public T findOne(Connection connection, T entity, String strategyName)
    {
        return strategyOperation.findOne(connection, entity, strategyName);
    }
    
    @Override
    public List<T> findAll(Connection connection, T param, String strategyName)
    {
        return strategyOperation.findAll(connection, param, strategyName);
    }
    
    @Override
    public List<T> findPage(Connection connection, T param, Page page, PageParse pageParse, String strategyName)
    {
        return strategyOperation.findPage(connection, param, page, pageParse, strategyName);
    }
    
}
