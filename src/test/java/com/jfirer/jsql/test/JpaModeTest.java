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
        user.setState(null);
        user.setName("linbin");
        user.setB(false);
        user.setStringEnum(User.StringEnum.v2);
        session.save(user);
        session.close();
        sqlSession = sessionFactory.openSession();
    }

    @Mapper
    public static interface UserOp extends Repository<User>
    {
        public User findByAge(int age);
    }

    @Test
    public void test()
    {
        UserOp userOp = sqlSession.getMapper(UserOp.class);
        User   user   = userOp.findByAge(12);
        Assert.assertEquals(18, user.getLength().intValue());
    }
}
