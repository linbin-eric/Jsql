package com.jfireframework.sql.dbstructure.impl;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.sql.dbstructure.Structure;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;
import com.jfireframework.sql.metadata.TableMetaData.FieldDesc;

public abstract class AbstractDBStructure implements Structure
{
    protected static final Logger logger = LoggerFactory.getLogger(Structure.class);
    
    @Override
    public void createTable(DataSource dataSource, TableMetaData[] metaDatas) throws SQLException
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            for (TableMetaData metaData : metaDatas)
            {
                if (metaData.getIdInfo() == null || metaData.editable() == false)
                {
                    continue;
                }
                _createTable(connection, metaData);
            }
            connection.commit();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        finally
        {
            if (connection != null)
            {
                connection.close();
            }
        }
        
    }
    
    protected String getDesc(MapField fieldInfo, TableMetaData tableMetaData)
    {
        FieldDesc fieldDesc = tableMetaData.getFieldDesc(fieldInfo);
        if (StringUtil.isNotBlank(fieldDesc.getDesc()))
        {
            return fieldDesc.getJdbcType().name() + "(" + fieldDesc.getDesc() + ")";
        }
        else
        {
            return fieldDesc.getJdbcType().name();
        }
    }
    
    protected abstract void _createTable(Connection connection, TableMetaData tableMetaData) throws SQLException;
    
}
