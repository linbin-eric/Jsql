package com.jfireframework.sql.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.uniqueid.Uid;
import com.jfireframework.sql.annotation.SeqId;
import com.jfireframework.sql.interceptor.SqlPreInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.resultsettransfer.field.MapField;

public class OracleDAO<T> extends BaseDAO<T>
{
    private SqlAndFields  seqInsertInfo;
    private SqlAndFields  insertInfo;
    private final boolean useSeq;
    private String[]      returnKey;
    
    public OracleDAO(TableMetaData metaData, SqlPreInterceptor[] preInterceptors, Uid uid)
    {
        super(metaData, preInterceptors, uid);
        useSeq = idField.getField().isAnnotationPresent(SeqId.class) ? true : false;
    }
    
    @Override
    public void batchInsert(List<T> entitys, Connection connection)
    {
        if (useUid || useSeq == false)
        {
            for (SqlPreInterceptor each : preInterceptors)
            {
                each.preIntercept(insertInfo.getSql(), entitys);
            }
            PreparedStatement pStat = null;
            try
            {
                pStat = connection.prepareStatement(insertInfo.getSql());
                for (Object entity : entitys)
                {
                    int index = 1;
                    if (useUid)
                    {
                        Object idValue = unsafe.getObject(entity, idOffset);
                        if (idValue == null)
                        {
                            switch (idType)
                            {
                                case LONG:
                                    unsafe.putObject(entity, idOffset, Long.valueOf(uid.generateLong()));
                                    break;
                                case STRING:
                                    unsafe.putObject(entity, idOffset, uid.generateDigits());
                                    break;
                                default:
                                    throw new IllegalArgumentException();
                            }
                        }
                    }
                    for (MapField field : insertInfo.getFields())
                    {
                        field.setStatementValue(pStat, entity, index);
                        index++;
                    }
                    pStat.addBatch();
                }
                pStat.executeBatch();
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
        else if (useSeq)
        {
            for (SqlPreInterceptor each : preInterceptors)
            {
                each.preIntercept(seqInsertInfo.getSql(), entitys);
            }
            PreparedStatement pStat = null;
            try
            {
                pStat = connection.prepareStatement(seqInsertInfo.getSql());
                for (Object entity : entitys)
                {
                    int index = 1;
                    for (MapField field : seqInsertInfo.getFields())
                    {
                        field.setStatementValue(pStat, entity, index);
                        index++;
                    }
                    pStat.addBatch();
                }
                pStat.executeBatch();
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
    protected void insert(T entity, Object idValue, Connection connection)
    {
        PreparedStatement pStat = null;
        SqlAndFields sqlAndFields = null;
        try
        {
            if (useUid || useSeq == false)
            {
                if (useUid && idValue == null)
                {
                    switch (idType)
                    {
                        case LONG:
                            idValue = Long.valueOf(uid.generateLong());
                            unsafe.putObject(entity, idOffset, idValue);
                            break;
                        case STRING:
                            idValue = uid.generateDigits();
                            unsafe.putObject(entity, idOffset, idValue);
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                }
                if (useUid == false)
                {
                    pStat = idValue == null ? connection.prepareStatement(insertInfo.getSql(), Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(insertInfo.getSql());
                }
                else
                {
                    pStat = connection.prepareStatement(insertInfo.getSql());
                }
                sqlAndFields = insertInfo;
            }
            else if (useSeq)
            {
                if (idValue != null)
                {
                    pStat = connection.prepareStatement(insertInfo.getSql());
                    sqlAndFields = insertInfo;
                }
                else
                {
                    pStat = connection.prepareStatement(seqInsertInfo.getSql(), returnKey);
                    sqlAndFields = seqInsertInfo;
                }
            }
            for (SqlPreInterceptor each : preInterceptors)
            {
                each.preIntercept(sqlAndFields.getSql(), entity);
            }
            int index = 1;
            for (MapField each : sqlAndFields.getFields())
            {
                each.setStatementValue(pStat, entity, index);
                index++;
            }
            pStat.executeUpdate();
            if (useUid == false && idValue == null)
            {
                ResultSet resultSet = pStat.getGeneratedKeys();
                if (resultSet.next())
                {
                    switch (idType)
                    {
                        case INT:
                            unsafe.putObject(entity, idOffset, resultSet.getInt(1));
                            break;
                        case LONG:
                            unsafe.putObject(entity, idOffset, resultSet.getLong(1));
                            break;
                        case STRING:
                            unsafe.putObject(entity, idOffset, resultSet.getString(1));
                            break;
                    }
                }
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
    
}
