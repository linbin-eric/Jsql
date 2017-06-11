package com.jfireframework.sql.dao.impl;

import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.sql.interceptor.SqlInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.resultsettransfer.field.MapField;
import com.jfireframework.sql.session.ExecSqlTemplate;
import com.jfireframework.sql.session.SqlSession;

public class MysqlDAO<T> extends BaseDAO<T>
{
    protected SqlAndFields insertInfo;
    
    public MysqlDAO(TableMetaData<?> metaData, SqlInterceptor[] sqlInterceptors)
    {
        super(metaData, sqlInterceptors);
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
    }
    
    @Override
    protected void insert(T entity, Object idValue, SqlSession session)
    {
        if (idValue == null)
        {
            Object pk = ExecSqlTemplate.insert(idType, pkName, sqlInterceptors, session.getConnection(), insertInfo.getSql(), parseParam(insertInfo.getFields(), entity));
            unsafe.putObject(entity, idOffset, pk);
        }
        else
        {
            session.update(insertInfo.getSql(), parseParam(insertInfo.getFields(), entity));
        }
    }
    
    @Override
    public void batchInsert(List<T> entitys, SqlSession session)
    {
        Object[] array = new Object[entitys.size()];
        int index = 0;
        for (Object entity : entitys)
        {
            array[index] = parseParam(insertInfo.getFields(), entity);
            index += 1;
        }
        ExecSqlTemplate.batchInsert(sqlInterceptors, session.getConnection(), insertInfo.getSql(), array);
    }
    
}
