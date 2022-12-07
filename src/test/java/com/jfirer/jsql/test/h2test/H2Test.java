package com.jfirer.jsql.test.h2test;

import com.jfirer.baseutil.TRACEID;
import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.metadata.TableMode;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.session.SqlSession;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class H2Test
{
    private static final Logger logger = LoggerFactory.getLogger(H2Test.class);

    /**
     * 表创建测试
     *
     * @throws SQLException
     */
    @Test
    @Ignore
    public void test() throws SQLException
    {
        String           traceId    = TRACEID.newTraceId();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        createTable(traceId, dataSource);
        createTable(traceId, dataSource);
    }

    private void createTable(String traceId, HikariDataSource dataSource) throws SQLException
    {
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        config.setScanPackage("com.jfirer.jsql.test.h2test");
        config.setTableMode(TableMode.CREATE);
        SessionFactory sessionFactory = config.build();
        SqlSession     session        = sessionFactory.openSession();
        Model insert = Model.insert(H2Table.class)//
                            .insert("col1", 1)//
                            .insert("col2", 23l)//
                            .insert("col3", 2.36f)//
                            .insert("col4", 5.645d)//
                            .insert("col5", "1212")//
                            .insert("col11", new byte[]{1, 2, 3, 4});
        session.insert(insert);
        H2Table one = session.findOne(Model.query(H2Table.class).where("col2", 23l));
        assertEquals(1, one.getCol1());
        assertEquals("1212", one.getCol5());
    }
}
