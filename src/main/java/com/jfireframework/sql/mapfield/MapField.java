package com.jfireframework.sql.mapfield;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.jfireframework.sql.util.JdbcType;

public interface MapField
{
    FieldOperator valueFetcher();
    
    /**
     * 从resultset通过名称获取值，并且设置到对象中
     * 
     * @param entity
     * @param resultSet
     * @throws SQLException
     */
    void setEntityValue(Object entity, ResultSet resultSet) throws SQLException;
    
    /**
     * 获得该属性的值
     * 
     * @param entity
     * @return
     */
    Object fieldValue(Object entity);
    
    /**
     * 获取该属性所对应的数据库字段名称
     * 
     * @return
     */
    String getColName();
    
    /**
     * 返回该属性的名字
     * 
     * @return
     */
    String getFieldName();
    
    JdbcType getJdbcType();
    
    String getDesc();
    
    /**
     * 返回原始的field对象
     * 
     * @return
     */
    Field getField();
    
}
