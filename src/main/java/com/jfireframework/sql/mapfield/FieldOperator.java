package com.jfireframework.sql.mapfield;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface FieldOperator
{
    void initialize(Field field);
    
    /**
     * 获取结果集中的值，并且设置到bean实例中。
     * 
     * @param entity bean实例
     * @param field 特定的field
     * @param dbColName 对应的数据库列名称
     * @param offset field的偏移量
     * @param resultSet
     * @throws SQLException
     */
    void setEntityValue(Object entity, Field field, String dbColName, long offset, ResultSet resultSet) throws SQLException;
    
    /**
     * 获取该属性的值
     * 
     * @param entity bean实例
     * @param field field对象
     * @param offset 该field对象在类中偏移量
     * @return
     */
    Object fieldValue(Object entity, Field field, long offset);
}
