package com.jfirer.jsql.dbstructure;

import com.jfirer.jsql.metadata.TableEntityInfo;
import com.jfirer.jsql.metadata.TableMode;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public interface SchemaAdjustment
{
    AtomicInteger count = new AtomicInteger(0);

    void adjust(TableMode mode, DataSource dataSource, Set<TableEntityInfo> tableEntityInfos) throws SQLException;
}
