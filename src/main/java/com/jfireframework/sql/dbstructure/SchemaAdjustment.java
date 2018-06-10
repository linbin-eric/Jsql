package com.jfireframework.sql.dbstructure;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import com.jfireframework.sql.util.TableEntityInfo;
import com.jfireframework.sql.util.TableMode;

public interface SchemaAdjustment
{
    static AtomicInteger count = new AtomicInteger(0);
    
    void adjust(TableMode mode, DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException;
    
    // void createTable(DataSource dataSource, Set<TableEntityInfo>
    // tableEntityInfos) throws SQLException;
    
    // void updateTable(DataSource dataSource, Set<TableEntityInfo>
    // tableEntityInfos) throws SQLException;
}
