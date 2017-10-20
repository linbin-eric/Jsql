package com.jfireframework.sql.dbstructure.impl;

import java.sql.Connection;
import java.sql.SQLException;
import com.jfireframework.sql.mapfield.MapField;
import com.jfireframework.sql.metadata.TableMetaData;

public class H2DBStructure extends AbstractDBStructure
{
    public H2DBStructure(String schema)
    {
        super(schema);
    }
    
    @Override
    protected void differentiatedUpdate(Connection connection, TableMetaData tableMetaData) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected String buildCreateTableSql(TableMetaData tableMetaData)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected void deleteExistTable(Connection connection, TableMetaData metaData) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected boolean checkIfTableExists(Connection connection, TableMetaData metaData) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    protected String getDbColumnDataType(Connection connection, String tableName, MapField each) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected void updateColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void deletePkConstraint(Connection connection, TableMetaData tableMetaData) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void addColumn(Connection connection, TableMetaData tableMetaData, String tableName, MapField each) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void addPKConstraint(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void deleteUnExistColumns(Connection connection, TableMetaData tableMetaData, String tableName) throws SQLException
    {
        // TODO Auto-generated method stub
        
    }
    
}
