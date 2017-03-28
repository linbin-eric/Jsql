package com.jfireframework.sql.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.uniqueid.Uid;
import com.jfireframework.sql.interceptor.SqlPreInterceptor;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.resultsettransfer.field.MapField;

public class StandardDAO<T> extends BaseDAO<T>
{
    protected SqlAndFields insertInfo;
    private SqlAndFields   identityInsertInfo;
    
    public StandardDAO(TableMetaData metaData, SqlPreInterceptor[] preInterceptors, Uid uid)
    {
        super(metaData, preInterceptors, uid);
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
    protected void insert(T entity, Object idValue, Connection connection)
    {
        PreparedStatement pStat = null;
        SqlAndFields sqlAndFields = null;
        try
        {
            if (useUid)
            {
                if (idValue == null)
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
                pStat = connection.prepareStatement(insertInfo.getSql());
                sqlAndFields = insertInfo;
            }
            else
            {
                if (idValue != null)
                {
                    pStat = connection.prepareStatement(insertInfo.getSql());
                    sqlAndFields = insertInfo;
                }
                else
                {
                    pStat = connection.prepareStatement(identityInsertInfo.getSql(), Statement.RETURN_GENERATED_KEYS);
                    sqlAndFields = identityInsertInfo;
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
    
    @Override
    public void batchInsert(List<T> entitys, Connection connection)
    {
        if (useUid)
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
        else
        {
            for (SqlPreInterceptor each : preInterceptors)
            {
                each.preIntercept(identityInsertInfo.getSql(), entitys);
            }
            PreparedStatement pStat = null;
            try
            {
                pStat = connection.prepareStatement(identityInsertInfo.getSql());
                for (Object entity : entitys)
                {
                    int index = 1;
                    for (MapField field : identityInsertInfo.getFields())
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
    
}
