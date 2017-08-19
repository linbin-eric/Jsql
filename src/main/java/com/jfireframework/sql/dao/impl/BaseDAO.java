package com.jfireframework.sql.dao.impl;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.sql.SqlSession;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.dao.StrategyOperation;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.resultsettransfer.impl.BeanTransfer;
import com.jfireframework.sql.util.IdType;
import com.jfireframework.sql.util.JdbcTypeDictionary;
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
    protected final static Unsafe        unsafe = ReflectUtil.getUnsafe();
    protected final String               tableName;
    protected final SqlAndFields         getInfo;
    protected final SqlAndFields         getInShareInfo;
    protected final SqlAndFields         getForUpdateInfo;
    protected final SqlAndFields         updateInfo;
    protected final String               deleteSql;
    protected static final Logger        LOGGER = LoggerFactory.getLogger(BaseDAO.class);
    protected final StrategyOperation<T> strategyOperation;
    protected final ResultSetTransfer    transfer;
    protected final String[]             pkName;
    protected final SqlInterceptor[]     sqlInterceptors;
    
    @SuppressWarnings({ "unchecked" })
    public BaseDAO(TableMetaData metaData, SqlInterceptor[] sqlInterceptors, JdbcTypeDictionary jdbcTypeDictionary)
    {
        this.entityClass = (Class<T>) metaData.getEntityClass();
        this.sqlInterceptors = sqlInterceptors;
        transfer = new BeanTransfer();
        transfer.initialize(entityClass);
        tableName = entityClass.getAnnotation(TableEntity.class).name();
        MapField[] allMapFields = metaData.getFieldInfos();
        MapField t_id = null;
        for (MapField mapField : allMapFields)
        {
            if (mapField.getField().isAnnotationPresent(Id.class))
            {
                t_id = mapField;
            }
        }
        Field t_idField = t_id.getField();
        idType = getIdType(t_idField);
        idField = t_id;
        pkName = new String[] { idField.getColName() };
        idOffset = unsafe.objectFieldOffset(t_idField);
        updateInfo = buildUpdate(allMapFields, idField);
        getInfo = buildGet(allMapFields, idField);
        getForUpdateInfo = buildGetForUpdate(allMapFields, idField);
        getInShareInfo = buildGetInShare(allMapFields, idField);
        useForSelf(allMapFields, idField);
        deleteSql = "delete from " + tableName + " where " + idField.getColName() + "=?";
        strategyOperation = new StrategyOperationImpl<T>(entityClass, allMapFields);
    }
    
    protected abstract void useForSelf(MapField[] fields, MapField idField);
    
    protected SqlAndFields buildGet(MapField[] fields, MapField idField)
    {
        List<MapField> getFields = new LinkedList<MapField>();
        StringCache cache = new StringCache();
        /******** 生成getSql ******/
        cache.clear();
        cache.append("select ");
        for (MapField each : fields)
        {
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
            if (each == idField)
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
    
    @Override
    public int delete(Object entity, SqlSession session)
    {
        return session.update(deleteSql, unsafe.getObject(entity, idOffset));
    }
    
    @Override
    public T getById(Object pk, SqlSession session)
    {
        return session.query(transfer, getInfo.getSql(), pk);
    }
    
    @Override
    public void save(T entity, SqlSession session)
    {
        Object idValue = unsafe.getObject(entity, idOffset);
        if (idValue == null)
        {
            insert(entity, null, session);
        }
        else
        {
            update(entity, session);
        }
        
    }
    
    protected Object[] parseParam(MapField[] fields, Object entity)
    {
        Object[] params = new Object[fields.length];
        for (int i = 0; i < params.length; i++)
        {
            params[i] = fields[i].fieldValue(entity);
        }
        return params;
    }
    
    @Override
    public int update(T entity, SqlSession session)
    {
        return session.update(updateInfo.getSql(), parseParam(updateInfo.getFields(), entity));
    }
    
    protected abstract void insert(T entity, Object idValue, SqlSession session);
    
    @Override
    public void insert(T entity, SqlSession session)
    {
        Object idValue = unsafe.getObject(entity, idOffset);
        insert(entity, idValue, session);
    }
    
    @Override
    public T getById(Object pk, SqlSession session, LockMode mode)
    {
        String sql = mode == LockMode.SHARE ? getInShareInfo.getSql() : getForUpdateInfo.getSql();
        return session.query(transfer, sql, pk);
    }
    
    @Override
    public int deleteAll(SqlSession session)
    {
        return session.update("delete from " + tableName);
    }
    
    @Override
    public int update(SqlSession session, String strategy, Object... params)
    {
        return strategyOperation.update(session, strategy, params);
    }
    
    @Override
    public int delete(SqlSession session, String strategy, Object... params)
    {
        return strategyOperation.delete(session, strategy, params);
    }
    
    @Override
    public T findOne(SqlSession session, String strategy, Object... params)
    {
        return strategyOperation.findOne(session, strategy, params);
    }
    
    @Override
    public List<T> findAll(SqlSession session, String strategy, Object... params)
    {
        return strategyOperation.findAll(session, strategy, params);
    }
    
    @Override
    public List<T> findPage(SqlSession session, Page page, String strategy, Object... params)
    {
        return strategyOperation.findPage(session, page, strategy, params);
    }
    
    @Override
    public int count(SqlSession session, String strategy, Object... params)
    {
        return strategyOperation.count(session, strategy, params);
    }
    
}
