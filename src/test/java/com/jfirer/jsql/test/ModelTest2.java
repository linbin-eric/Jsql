package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.dialect.impl.H2Dialect;
import com.jfirer.jsql.model.Model;
import com.jfirer.jsql.model.Param;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.SqlLog;
import com.jfirer.jsql.test.vo.User;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static com.jfirer.jsql.test.CURDTest.user2TableDml;
import static com.jfirer.jsql.test.CURDTest.userTableDml;

public class ModelTest2
{
    private SessionFactory       sessionFactory;
    private SessionfactoryConfig config;
    SqlSession sqlSession;

    @After
    public void after()
    {
        sqlSession.close();
    }

    @Before
    public void before() throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        config = new SessionfactoryConfig();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:orderdb");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        config.setDataSource(dataSource);
        config.setDialect(new H2Dialect((preparedStatement, i, value) -> {
            if (value instanceof User.StringEnum stringEnum)
            {
                preparedStatement.setString(i, stringEnum.name());
            }
            else if (value instanceof Enum<?> enum1)
            {
                preparedStatement.setInt(i, enum1.ordinal());
            }
            else
            {
                preparedStatement.setObject(i, value);
            }
        }));
        config.addSqlExecutor(new SqlLog());
        sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        session.execute("DROP TABLE IF EXISTS user", new LinkedList<>());
        session.execute("DROP TABLE IF EXISTS user2", new LinkedList<>());
        session.execute(userTableDml, new LinkedList<>());
        session.execute(user2TableDml, new LinkedList<>());
        User user = new User();
        user.setAge(12);
        user.setName("lin");
        user.setLength(18);
        user.setState(User.State.off);
        user.setB(true);
        user.setStringEnum(User.StringEnum.v1);
        session.save(user);
        user.setId(null);
        user.setAge(13);
        user.setState(User.State.off);
        user.setName("linbin");
        user.setB(false);
        user.setStringEnum(User.StringEnum.v2);
        session.save(user);
        session.close();
        sqlSession = sessionFactory.openSession();
    }

    @Test
    public void test()
    {
        User user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getAge, 12)));
        Assert.assertTrue(user.isB());
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getId, 1)));
        Assert.assertTrue(user.isB());
        sqlSession.execute(Model.update(User.class).set(User::getAge, 13).where(Param.eq(User::getId, 1)));
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getId, 1)));
        Assert.assertEquals(13, user.getAge());
        sqlSession.execute(Model.update(User.class).set(User::getAge, 12).where(Param.eq(User::getId, 1)));
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getAge, 12).and(Param.eq(User::getName, "lin"))));
        Assert.assertTrue(user.isB());
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.bt(User::getAge, 12)));
        Assert.assertEquals("linbin", user.getName());
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.startWith(User::getName, "linb")));
        Assert.assertEquals(13, user.getAge());
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.endWith(User::getName, "nbin")));
        Assert.assertEquals(13, user.getAge());
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.contain(User::getName, "nb")));
        Assert.assertEquals(13, user.getAge());
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.be(User::getAge, 13)));
        Assert.assertEquals("linbin", user.getName());
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.lt(User::getAge, 13)));
        Assert.assertEquals("lin", user.getName());
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.le(User::getAge, 12)));
        Assert.assertEquals("lin", user.getName());
        user = (User) sqlSession.findList(Model.selectAll().from(User.class).where(Param.in(User::getAge, 9, 12))).get(0);
        Assert.assertEquals("lin", user.getName());
        user = (User) sqlSession.findList(Model.selectAll().from(User.class).where(Param.notIn(User::getAge, 9, 13))).get(0);
        Assert.assertEquals("lin", user.getName());
        user = (User) sqlSession.findList(Model.selectAll().from(User.class).where(Param.eq(User::getState, User.State.off.ordinal())).orderBy(User::getAge, true)).get(0);
        Assert.assertEquals("linbin", user.getName());
        sqlSession.execute(Model.update(User.class).set(User::getAge, 14).where(Param.eq(User::getAge, 12)));
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getAge, 14)));
        Assert.assertEquals("lin", user.getName());
        System.out.println(user.getName() + ":" + user.getAge());
        int result = sqlSession.execute(Model.update(User.class).set(User::getAge, 1).set(User::getName, "linnew").where(Param.eq(User::getAge, 14)));
        Assert.assertEquals(1, result);
        user = sqlSession.findOne(Model.selectAll().from(User.class).where(Param.eq(User::getAge, 1)));
        Assert.assertNotNull(user);
    }
}
