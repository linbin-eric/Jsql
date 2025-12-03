package cc.jfire.jsql.test;

import cc.jfire.jsql.SessionFactory;
import cc.jfire.jsql.SessionFactoryConfig;
import cc.jfire.jsql.annotation.TableName;
import cc.jfire.jsql.annotation.TableDef;
import cc.jfire.jsql.model.Model;
import cc.jfire.jsql.model.Param;
import cc.jfire.jsql.session.SqlSession;
import cc.jfire.jsql.test.vo.SqlLog;
import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Slf4j
public class SameNameColumnTest
{
    private SessionFactory sessionFactory;

    @Before
    public void before()
    {
        SessionFactoryConfig config     = new SessionFactoryConfig();
        HikariDataSource     dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://mysql.orb.local:3306/test");
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        config.setDataSource(dataSource);
        config.addSqlExecutor(new SqlLog());
        sessionFactory = config.build();
    }

    @Data
    public static class UserVo
    {
        @TableName("se_user")
        private int    id;
        private int    targetId;
        @TableName("user")
        private String name;
        private String email;
    }

    @TableDef("user")
    @Data
    public static class User1
    {
        private int    id;
        private String name;
        private int    targetId;
        private int    age;
        private String email;
    }

    @TableDef("se_user")
    @Data
    public static class SeUser
    {
        private int    id;
        private String name;
        private int    targetId;
        private int    userId;
    }

    @Test
    @Ignore
    public void test()
    {
        try (SqlSession session = sessionFactory.openSession())
        {
            UserVo vo = session.findOne(Model.selectAll().addSelect(SeUser::getId, SeUser::getName)//
                                             .addSelect(User1::getId, User1::getName, User1::getEmail)//
                                             .from(SeUser.class, "seU").leftJoin(User1.class, "u1")//
                                             .on(Param.eq(SeUser::getTargetId, User1::getTargetId))//
                                             .where(Param.eq(User1::getTargetId, 1))//
                                             .returnType(UserVo.class));
            log.debug("内容:{}", vo);
        }
    }
}
