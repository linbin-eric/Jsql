package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.dialect.impl.H2Dialect;
import com.jfirer.jsql.mapper.Mapper;
import com.jfirer.jsql.mapper.Repository;
import com.jfirer.jsql.metadata.TableMode;
import com.jfirer.jsql.session.SqlSession;
import com.jfirer.jsql.test.vo.SqlLog;
import com.jfirer.jsql.test.vo.User;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JpaModeTest
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
        config.setClassLoader(MapperTest.class.getClassLoader());
        config.setTableMode(TableMode.CREATE);
        config.setDialect(new H2Dialect()
        {
            protected void setUnDefinedType(PreparedStatement preparedStatement, int i, Object value) throws SQLException
            {
                if (value instanceof User.StringEnum)
                {
                    User.StringEnum stringEnum = (User.StringEnum) value;
                    preparedStatement.setString(i, stringEnum.name());
                }
                else if (value instanceof Enum<?>)
                {
                    Enum<?> enum1 = (Enum<?>) value;
                    preparedStatement.setInt(i, enum1.ordinal());
                }
                else
                {
                    preparedStatement.setObject(i, value);
                }
            }
        });
        config.setScanPackage("com.jfirer.jsql.test:in~*$UserOp;com.jfirer.jsql.test.vo");
        config.addSqlExecutor(new SqlLog());
        sessionFactory = config.build();
        SqlSession session = sessionFactory.openSession();
        User       user    = new User();
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
        UserOp userOp = sqlSession.getMapper(UserOp.class);
        User   user   = userOp.findByAge(12);
        Assert.assertTrue(user.isB());
        user = userOp.findByPk(1);
        Assert.assertTrue(user.isB());
        userOp.updateAgeByPk(13,1);
        user = userOp.findByPk(1);
        Assert.assertEquals(13,user.getAge());
        userOp.updateAgeByPk(12,1);
        user = userOp.findByAgeAndByName(12, "lin");
        Assert.assertTrue(user.isB());
        user = userOp.findByAgeGreaterThan(12);
        Assert.assertEquals("linbin", user.getName());
        user = userOp.findByNameStartingWith("linb");
        Assert.assertEquals(13, user.getAge());
        user = userOp.findByNameEndingWith("nbin");
        Assert.assertEquals(13, user.getAge());
        user = userOp.findByNameContaining("nb");
        Assert.assertEquals(13, user.getAge());
        user = userOp.findByAgeGreaterThanEqual(13);
        Assert.assertEquals("linbin", user.getName());
        user = userOp.findByAgeLessThan(13);
        Assert.assertEquals("lin", user.getName());
        user = userOp.findByAgeLessThanEqual(12);
        Assert.assertEquals("lin", user.getName());
        user = userOp.findByAgeIn(new int[]{9, 12}).get(0);
        Assert.assertEquals("lin", user.getName());
        user = userOp.findByAgeNotIn(new int[]{9, 13}).get(0);
        Assert.assertEquals("lin", user.getName());
        user = userOp.findByStateOrderByAgeDesc(User.State.off.ordinal()).get(0);
        Assert.assertEquals("linbin", user.getName());
        userOp.updateAgeByAge(14, 12);
        user = userOp.findByAge(14);
        Assert.assertEquals("lin", user.getName());
        System.out.println(user.getName() + ":" + user.getAge());
        int result = userOp.updateAgeAndNameByAge(1, "linnew", 14);
        Assert.assertEquals(1, result);
        user = userOp.findByAge(1);
        Assert.assertNotNull(user);
    }

    @Mapper
    public static interface UserOp extends Repository<User>
    {
        User findByAge(int age);

        User findByAgeAndByName(int age, String name);

        User findByAgeGreaterThan(int age);

        User findByNameStartingWith(String name);

        User findByNameEndingWith(String name);

        User findByNameContaining(String name);

        User findByAgeGreaterThanEqual(int age);

        User findByAgeLessThan(int age);

        User findByAgeLessThanEqual(int age);

        List<User> findByAgeIn(int[] ages);

        List<User> findByAgeNotIn(int[] ages);

        List<User> findByStateOrderByAgeDesc(int state);

        void updateAgeByAge(int newAge, int oldAge);

        int updateAgeAndNameByAge(int newAge, String name, int oldAge);

        User findByPk(int id);

        void updateAgeByPk(int age, int id);
    }
}
