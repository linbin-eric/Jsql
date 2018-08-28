package com.jfireframework.sql.dbstructure;

import com.jfireframework.sql.metadata.TableEntityInfo;
import com.jfireframework.sql.metadata.TableMode;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public interface SchemaAdjustment
{
    static AtomicInteger count = new AtomicInteger(0);

    void adjust(TableMode mode, DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException;

}
