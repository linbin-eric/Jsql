package com.jfireframework.sql.dao.impl;

import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.session.SqlSession;

public class StandardDAO<T> extends BaseDAO<T>
{
    protected SqlAndFields insertInfo;
    private SqlAndFields   identityInsertInfo;
    
    public StandardDAO(TableMetaData metaData)
    {
        super(metaData);
    }
    
    @Override
    protected void useForSelf(MapField[] fields, MapField idField)
    {
        List<MapField> insertFields = new ArrayList<MapField>();
        StringCache cache = new StringCache();
        cache.append("insert into ").append(tableName).append(" ( ");
        cache.append(idField.getColName()).appendComma();
        insertFields.add(idField);
        int count = 0;
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
        String identityStr = "DEFAULT";
        cache.deleteLast().append(") values ( " + identityStr + ",");
        cache.appendStrsByComma("?", count);
        if (cache.isCommaLast())
        {
            cache.deleteLast();
        }
        cache.append(')');
        String sql = cache.toString();
        insertInfo = new SqlAndFields(sql.replace(identityStr, "?"), insertFields.toArray(new MapField[insertFields.size()]));
        LOGGER.debug("为表{},类{}创建的插入语句是{}", tableName, entityClass.getName(), insertInfo.getSql());
        insertFields.remove(0);
        identityInsertInfo = new SqlAndFields(cache.toString(), insertFields.toArray(new MapField[insertFields.size()]));
        LOGGER.debug("为表{},类{}创建的插入语句是{}", tableName, entityClass.getName(), identityInsertInfo.getSql());
    }
    
    @Override
    protected void insert(T entity, Object idValue, SqlSession session)
    {
        if (idValue != null)
        {
            session.update(insertInfo.getSql(), parseParam(insertInfo.getFields(), entity));
        }
        else
        {
            Object pk = session.insertWithReturnPKValue(idType, pkName, identityInsertInfo.getSql(), parseParam(identityInsertInfo.getFields(), entity));
            unsafe.putObject(entity, idOffset, pk);
        }
    }
    
    @Override
    public void batchInsert(List<T> entitys, SqlSession session)
    {
        Object[] array = new Object[entitys.size()];
        for (Object entity : entitys)
        {
            int index = 1;
            array[index] = parseParam(identityInsertInfo.getFields(), entity);
        }
        session.batchInsert(identityInsertInfo.getSql(), array);
    }
    
}
