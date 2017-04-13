package com.jfireframework.sql.dao.impl;

import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.annotation.SeqId;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.session.SqlSession;

public class OracleDAO<T> extends BaseDAO<T>
{
    private SqlAndFields  seqInsertInfo;
    private SqlAndFields  insertInfo;
    private final boolean useSeq;
    private String[]      returnKey;
    
    public OracleDAO(TableMetaData metaData)
    {
        super(metaData);
        useSeq = idField.getField().isAnnotationPresent(SeqId.class) ? true : false;
    }
    
    @Override
    public void batchInsert(List<T> entitys, SqlSession session)
    {
        Object[] array = new Object[entitys.size()];
        for (Object entity : entitys)
        {
            int index = 1;
            array[index] = parseParam(insertInfo.getFields(), entity);
        }
        if (useSeq == false)
        {
            session.batchInsert(insertInfo.getSql(), array);
        }
        else
        {
            session.batchInsert(seqInsertInfo.getSql(), array);
        }
    }
    
    @Override
    protected void useForSelf(MapField[] fields, MapField idField)
    {
        List<MapField> insertFields = new LinkedList<MapField>();
        StringCache cache = new StringCache();
        /******** 生成insertSql *******/
        cache.append("insert into ").append(tableName).append(" ( ");
        cache.append(idField.getColName()).appendComma();
        insertFields.add(idField);
        int count = 1;
        for (MapField each : fields)
        {
            if (each == idField || each.saveIgnore())
            {
                continue;
            }
            count++;
            insertFields.add(each);
            cache.append(each.getColName()).append(',');
        }
        cache.deleteLast().append(") values (");
        cache.appendStrsByComma("?", count);
        cache.append(')');
        insertInfo = new SqlAndFields(cache.toString(), insertFields.toArray(new MapField[insertFields.size()]));
        LOGGER.debug("为表{},类{}创建的插入语句是{}", tableName, entityClass.getName(), insertInfo.getSql());
        if (idField.getField().isAnnotationPresent(SeqId.class))
        {
            insertFields.clear();
            cache.clear();
            /******** 生成insertSql *******/
            cache.append("insert into ").append(tableName).append(" ( ");
            cache.append(idField.getColName()).appendComma();
            count = 0;
            for (MapField each : fields)
            {
                if (each == idField || each.saveIgnore())
                {
                    continue;
                }
                count++;
                insertFields.add(each);
                cache.append(each.getColName()).append(',');
            }
            String seqName = idField.getField().getAnnotation(SeqId.class).value();
            cache.deleteLast().append(") values (").append(seqName).append(".nextval,");
            cache.appendStrsByComma("?", count);
            if (cache.isCommaLast())
            {
                cache.deleteLast();
            }
            cache.append(')');
            seqInsertInfo = new SqlAndFields(cache.toString(), insertFields.toArray(new MapField[insertFields.size()]));
            LOGGER.debug("为表{},类{}创建的插入语句是{}", tableName, entityClass.getName(), seqInsertInfo.getSql());
            returnKey = new String[] { idField.getColName() };
        }
        
    }
    
    @Override
    protected void insert(T entity, Object idValue, SqlSession session)
    {
        String sql;
        Object[] params;
        boolean returnPk = false;
        if (idValue == null)
        {
            returnPk = true;
        }
        else
        {
            returnPk = false;
        }
        if (useSeq == false)
        {
            sql = insertInfo.getSql();
            params = parseParam(insertInfo.getFields(), entity);
        }
        else
        {
            if (idValue != null)
            {
                sql = insertInfo.getSql();
                params = parseParam(insertInfo.getFields(), entity);
            }
            else
            {
                sql = seqInsertInfo.getSql();
                params = parseParam(seqInsertInfo.getFields(), entity);
            }
        }
        if (returnPk)
        {
            Object pk = session.insertWithReturnPKValue(idType, returnKey, sql, params);
            unsafe.putObject(entity, idOffset, pk);
        }
        else
        {
            session.insert(sql, params);
        }
    }
    
}
