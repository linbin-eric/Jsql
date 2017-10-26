package com.jfireframework.sql.dao.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.verify.Verify;
import com.jfireframework.sql.dao.impl.MysqlDAO.GeneratePkStrategy;
import com.jfireframework.sql.dbstructure.column.ColumnType;
import com.jfireframework.sql.dbstructure.column.ColumnTypeDictionary;
import com.jfireframework.sql.dbstructure.name.ColumnNameStrategy;
import com.jfireframework.sql.idstrategy.AutoIncrement;
import com.jfireframework.sql.idstrategy.GenerateStringPk;
import com.jfireframework.sql.idstrategy.GenerateStringPk.StringGenerator;
import com.jfireframework.sql.mapfield.FieldOperator;
import com.jfireframework.sql.mapfield.FieldOperatorDictionary;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.session.ExecSqlTemplate;
import com.jfireframework.sql.util.ExecuteSqlInfo;

public class H2DAO extends BaseDAO
{
    protected GeneratePkStrategy generatePkStrategy;
    protected ExecuteSqlInfo     autoGeneratePkInsertInfo;
    
    @Override
    protected void setAutoGeneratePkInsertInfo()
    {
        Field field = pkColumn.getField();
        if (Number.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(AutoIncrement.class))
        {
            StringCache cache = new StringCache();
            cache.append("INSERT INTO ").append(tableName).append('(').append(pkColumn.getColName()).appendComma();
            for (MapField column : valueColumns)
            {
                cache.append(column.getColName()).appendComma();
            }
            cache.deleteLast().append(") VALUES(NULL,");
            for (int i = 0; i < valueColumns.length; i++)
            {
                cache.append("?").appendComma();
            }
            cache.deleteLast().append(")");
            autoGeneratePkInsertInfo = new ExecuteSqlInfo(cache.toString(), valueColumns);
            generatePkStrategy = GeneratePkStrategy.GENERATE_BY_DATABASE;
        }
        else if (field.getType() == String.class && field.isAnnotationPresent(GenerateStringPk.class))
        {
            StringCache cache = new StringCache();
            List<MapField> params = new ArrayList<MapField>();
            cache.append("INSERT INTO ").append(tableName).append('(').append(pkColumn.getColName()).appendComma();
            for (MapField column : valueColumns)
            {
                cache.append(column.getColName()).appendComma();
            }
            cache.deleteLast().append(") VALUES(?,");
            try
            {
                final StringGenerator stringGenerator = field.getAnnotation(GenerateStringPk.class).value().newInstance();
                params.add(new MapField() {
                    
                    @Override
                    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException
                    {
                        throw new UnsupportedOperationException();
                    }
                    
                    @Override
                    public void initialize(Field field, ColumnNameStrategy colNameStrategy, FieldOperatorDictionary fieldOperatorDictionary, ColumnTypeDictionary columnTypeDictionary)
                    {
                        throw new UnsupportedOperationException();
                    }
                    
                    @Override
                    public String getFieldName()
                    {
                        throw new UnsupportedOperationException();
                    }
                    
                    @Override
                    public Field getField()
                    {
                        throw new UnsupportedOperationException();
                    }
                    
                    @Override
                    public ColumnType getColumnType()
                    {
                        throw new UnsupportedOperationException();
                    }
                    
                    @Override
                    public String getColName()
                    {
                        throw new UnsupportedOperationException();
                    }
                    
                    @Override
                    public Object fieldValue(Object entity)
                    {
                        return stringGenerator.next();
                    }
                    
                    @Override
                    public FieldOperator fieldOperator()
                    {
                        throw new UnsupportedOperationException();
                    }
                });
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
            for (MapField column : valueColumns)
            {
                params.add(column);
                cache.append("?").appendComma();
            }
            cache.deleteLast().append(")");
            generatePkStrategy = GeneratePkStrategy.GENERATE_BY_APPLICATION;
        }
    }
    
    @Override
    protected Object autoGeneratePkInsert(Object entity, Connection connection)
    {
        Verify.notNull(generatePkStrategy, "generatePkStrategy为空时无法执行生成主键并插入。请检查{}", entityClass.getName());
        switch (generatePkStrategy)
        {
            case GENERATE_BY_APPLICATION:
                ExecSqlTemplate.insert(sqlInterceptors, connection, autoGeneratePkInsertInfo.getSql(), parseParam(autoGeneratePkInsertInfo.getColumns(), entity));
                break;
            case GENERATE_BY_DATABASE:
                ExecSqlTemplate.databasePkGenerateInsert(pkType, pkName, sqlInterceptors, connection, autoGeneratePkInsertInfo.getSql(), parseParam(autoGeneratePkInsertInfo.getColumns(), entity));
                break;
            default:
                break;
        }
    }
    
}
