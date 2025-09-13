package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionFactoryConfig;
import com.jfirer.jsql.mapper.Mapper;
import com.jfirer.jsql.mapper.Repository;
import com.jfirer.jsql.metadata.Page;
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
import java.util.List;

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
        SessionFactoryConfig config     = new SessionFactoryConfig();
        HikariDataSource     dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:orderdb");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        config.setDataSource(dataSource);
        config.addSqlExecutor(new SqlLog());
        sessionFactory = config.build();
        SqlSession sqlSession = sessionFactory.openSession();
        sqlSession.execute("DROP TABLE IF EXISTS user", new LinkedList<>());
        sqlSession.execute("DROP TABLE IF EXISTS user2", new LinkedList<>());
        sqlSession.execute(userTableDml, new LinkedList<>());
        sqlSession.execute(user2TableDml, new LinkedList<>());
    }

    @Test
    public void test()
    {
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
        user.setAge(12);
        user.setName("linbin");
        session.save(user);
        RepositoryOp repositoryOp = session.getMapper(RepositoryOp.class);
        User         linbin       = repositoryOp.findOne(Param.eq(User::getName, "linbin"));
        Assert.assertEquals(12, linbin.getAge());
        user.setId(null);
        user.setAge(14);
        session.save(user);
        List<User> list = repositoryOp.findList(Param.bt(User::getAge, 13));
        Assert.assertEquals(1, list.size());
        user.setId(null);
        user.setAge(20);
        repositoryOp.save(user);
        list = repositoryOp.findList(Param.bt(User::getAge, 13));
        Assert.assertEquals(2, list.size());
        user.setAge(40);
        repositoryOp.update(user);
        User one = repositoryOp.findOne(Param.eq(User::getId, user.getId()));
        Assert.assertEquals(40, one.getAge());
        repositoryOp.delete(Param.eq(User::getAge, 40));
        Assert.assertEquals(2, repositoryOp.count(Param.notEq(User::getId, 0)));
    }
    
    @Test
    public void testPage() {
        SqlSession session = sessionFactory.openSession();
        // 插入测试数据
        for (int i = 0; i < 20; i++) {
            User user = new User();
            user.setAge(10 + i);
            user.setName("user" + i);
            session.save(user);
        }
        
        RepositoryOp repositoryOp = session.getMapper(RepositoryOp.class);
        
        // 测试分页查询
        Page page = new Page();
        page.setOffset(0);
        page.setSize(10);
        
        List<User> list = repositoryOp.findList(null, page);
        Assert.assertEquals(10, list.size());
        
        // 测试第二页
        page.setOffset(10);
        list = repositoryOp.findList(null, page);
        Assert.assertEquals(10, list.size());
        
        // 测试带条件的分页查询
        page.setOffset(0);
        page.setSize(5);
        list = repositoryOp.findList(Param.bt(User::getAge, 15), page);
        Assert.assertEquals(5, list.size());
        // 验证数据正确性
        for (User user : list) {
            Assert.assertTrue(user.getAge() > 15);
        }
    }
}
