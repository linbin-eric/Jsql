package com.jfireframework.sql.dao.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.simplelog.ConsoleLogFactory;
import com.jfireframework.baseutil.simplelog.Logger;
import com.jfireframework.sql.annotation.FindBy;
import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.dao.Dao;
import com.jfireframework.sql.dao.LockMode;
import com.jfireframework.sql.dao.StrategyOperation;
import com.jfireframework.sql.dbstructure.name.ColNameStrategy;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.metadata.TableMetaData.FieldInfo;
import com.jfireframework.sql.page.Page;
import com.jfireframework.sql.resultsettransfer.FixBeanTransfer;
import com.jfireframework.sql.resultsettransfer.ResultSetTransfer;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.resultsettransfer.field.MapFieldBuilder;
import com.jfireframework.sql.session.SqlSession;
import com.jfireframework.sql.util.IdType;
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
    
    protected final Class<T>                        entityClass;
    // 代表数据库主键id的field
    protected final MapField                        idField;
    protected final long                            idOffset;
    protected final IdType                          idType;
    protected final static Unsafe                   unsafe             = ReflectUtil.getUnsafe();
    protected final String                          tableName;
    protected final SqlAndFields                    getInfo;
    protected final SqlAndFields                    getInShareInfo;
    protected final SqlAndFields                    getForUpdateInfo;
    protected final SqlAndFields                    updateInfo;
    protected final String                          deleteSql;
    protected static final Logger                   LOGGER             = ConsoleLogFactory.getLogger();
    protected final StrategyOperation<T>            strategyOperation;
    protected final Map<String, String>             findByMap          = new HashMap<String, String>();
    protected final Map<String, FixBeanTransfer<?>> findByTransfereMap = new HashMap<String, FixBeanTransfer<?>>();
    protected ResultSetTransfer<T>                  transfer;
    protected String[]                              pkName;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BaseDAO(TableMetaData metaData)
    {
        this.entityClass = (Class<T>) metaData.getEntityClass();
        transfer = new FixBeanTransfer(entityClass);
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
                findByTransfereMap.put(mapField.getFieldName(), new FixBeanTransfer<T>(entityClass));
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
        logSql();
        strategyOperation = new StrategyOperationImpl<T>(entityClass, allMapFields);
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
            try
            {
                params[i] = fields[i].statementValue(entity);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
    
    @SuppressWarnings("unchecked")
    @Override
    public T findBy(SqlSession session, Object param, String name)
    {
        String sql = findByMap.get(name);
        if (sql == null)
        {
            throw new NullPointerException("没有属性:" + name + "的findBy注解,请检查类:" + entityClass.getName());
        }
        return (T) session.query(findByTransfereMap.get(name), sql, param);
    }
    
    @Override
    public int deleteAll(SqlSession session)
    {
        return session.update("delete from " + tableName);
    }
    
    public int update(SqlSession session, String strategy, Object... params)
    {
        return strategyOperation.update(session, strategy, params);
    }
    
    public T findOne(SqlSession session, String strategy, Object... params)
    {
        return strategyOperation.findOne(session, strategy, params);
    }
    
    public List<T> findAll(SqlSession session, String strategy, Object... params)
    {
        return strategyOperation.findAll(session, strategy, params);
    }
    
    public List<T> findPage(SqlSession session, Page page, String strategy, Object... params)
    {
        return strategyOperation.findPage(session, page, strategy, params);
    }
    
}
