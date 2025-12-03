package cc.jfire.jsql.test;

import cc.jfire.jsql.SessionFactory;
import cc.jfire.jsql.SessionFactoryConfig;
import cc.jfire.jsql.annotation.AutoIncrement;
import cc.jfire.jsql.annotation.Pk;
import cc.jfire.jsql.annotation.TableDef;
import cc.jfire.jsql.session.SqlSession;
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
        SessionFactoryConfig config = new SessionFactoryConfig();
        config.setDataSource(dataSource);
        sessionFactory = config.build();
    }

    @Test
    @Ignore
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
