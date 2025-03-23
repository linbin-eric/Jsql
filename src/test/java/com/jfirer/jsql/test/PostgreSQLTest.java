package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.Pk;
import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.session.SqlSession;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PostgreSQLTest
{
    private SessionFactory sessionFactory;

    @Before
    public void before()
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setUsername("postgres");
        dataSource.setPassword("root");
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        sessionFactory = config.build();
    }

    @Test
    public void test()
    {
        try (SqlSession session = sessionFactory.openSession())
        {
            session.save(new Book().setName("book1"));
        }
    }

    @TableDef("book")
    @Data
    @Accessors(chain = true)
    public class Book
    {
        @Pk
        @AutoIncrement
        private Integer id;
        private String  name;
        private float[] embedding;
    }
}
