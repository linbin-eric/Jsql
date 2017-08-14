package com.jfireframework.sql.resultsettransfer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.util.List;
import com.jfireframework.sql.util.JdbcTypeDictionary;

public interface ResultSetTransfer
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface CustomTransfer
    {
        Class<? extends ResultSetTransfer> value();
    }
    
    void initialize(Class<?> type, JdbcTypeDictionary jdbcTypeDictionary);
    
    public Object transfer(ResultSet resultSet, String sql) throws Exception;
    
    public List<Object> transferList(ResultSet resultSet, String sql) throws Exception;
}
