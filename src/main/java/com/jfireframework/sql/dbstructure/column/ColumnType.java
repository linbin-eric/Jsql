package com.jfireframework.sql.dbstructure.column;

public interface ColumnType
{
    /**
     * 数据库字段的类型
     * 
     * @return
     */
    String type();
    
    /**
     * 该字段的描述
     * 
     * @return
     */
    String desc();
}
