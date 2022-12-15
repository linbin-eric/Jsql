package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.mapper.Mapper;
import com.jfirer.jsql.mapper.Repository;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.SqlLog;
import com.jfirer.jsql.test.vo.User;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static com.jfirer.jsql.test.CURDTest.user2TableDml;
import static com.jfirer.jsql.test.CURDTest.userTableDml;

public class RepositoryTest
{
    private SessionFactory sessionFactory;

    @Mapper
    public interface RepositoryOp extends Repository<User>
    {}

    @Before
    public void before()
    {
        SessionfactoryConfig config     = new SessionfactoryConfig();
        HikariDataSource     dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:orderdb");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        config.setDataSource(dataSource);
        config.setClassLoader(CURDTest.class.getClassLoader());
        config.setScanPackage("com.jfirer.jsql.test:in~*$RepositoryOp;com.jfirer.jsql.test.vo");
        config.addSqlExecutor(new SqlLog());
        sessionFactory = config.build();
        SqlSession sqlSession = sessionFactory.openSession();
        sqlSession.execute("DROP TABLE IF EXISTS user", new LinkedList<>());
        sqlSession.execute("DROP TABLE IF EXISTS user2", new LinkedList<>());
        sqlSession.execute(userTableDml, new LinkedList<>());
        sqlSession.execute(user2TableDml, new LinkedList<>());
    }

    @Test
    public void testFindOne()
    {
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
        user.setAge(12);
        user.setName("linbin");
        session.save(user);
        RepositoryOp repositoryOp = session.getMapper(RepositoryOp.class);
        User         linbin       = repositoryOp.findOne(Param.eq(User::getName, "linbin"));
        Assert.assertEquals(12, linbin.getAge());
    }
}
