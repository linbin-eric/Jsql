package com.jfireframework.sql.dao.impl;

import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.session.ExecSqlTemplate;
import com.jfireframework.sql.session.SqlSession;

public class StandardDAO<T> extends BaseDAO<T>
{
    protected SqlAndFields insertInfo;
    private SqlAndFields   identityInsertInfo;
    
    public StandardDAO(TableMetaData metaData, SqlInterceptor[] sqlInterceptors)
    {
        super(metaData, sqlInterceptors);
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
        LOGGER.trace("为表{},类{}创建的插入语句是{}", tableName, entityClass.getName(), insertInfo.getSql());
        insertFields.remove(0);
        identityInsertInfo = new SqlAndFields(cache.toString(), insertFields.toArray(new MapField[insertFields.size()]));
        LOGGER.trace("为表{},类{}创建的主键新增插入语句是{}", tableName, entityClass.getName(), identityInsertInfo.getSql());
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
            Object pk = ExecSqlTemplate.insert(idType, pkName, sqlInterceptors, session.getConnection(), identityInsertInfo.getSql(), parseParam(identityInsertInfo.getFields(), entity));
            unsafe.putObject(entity, idOffset, pk);
        }
    }
    
    @Override
    public void batchInsert(List<T> entitys, SqlSession session)
    {
        Object[] array = new Object[entitys.size()];
        int index = 0;
        for (Object entity : entitys)
        {
            array[index] = parseParam(identityInsertInfo.getFields(), entity);
            index += 1;
        }
        ExecSqlTemplate.batchInsert(sqlInterceptors, session.getConnection(), identityInsertInfo.getSql(), array);
    }
    
}
