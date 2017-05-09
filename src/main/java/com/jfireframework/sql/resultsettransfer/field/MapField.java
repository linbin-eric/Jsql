package com.jfireframework.sql.resultsettransfer.field;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface MapField
{
    /**
     * 从resultset通过名称获取值，并且设置到对象中
     * 
     * @param entity
     * @param resultSet
     * @throws SQLException
     */
    public void setEntityValue(Object entity, ResultSet resultSet) throws SQLException;
    
    /**
     * 获得该属性的值
     * 
     * @param entity
     * @return
     */
    public Object fieldValue(Object entity);
    
    /**
     * 获取该属性所对应的数据库字段名称
     * 
     * @return
     */
    public String getColName();
    
    /**
     * 该属性在保存或更新的时候是否会被忽略，该属性只针对DAO的CURD操作有效
     * 
     * @return
     */
    public boolean saveIgnore();
    
    /**
     * 该属性在读取的时候是否会被忽略，该属性只针对DAO的CURD操作有效
     * 
     * @return
     */
    public boolean loadIgnore();
    
    /**
     * 返回该属性的名字
     * 
     * @return
     */
    public String getFieldName();
    
    /**
     * 返回原始的field对象
     * 
     * @return
     */
    public Field getField();
    
}
