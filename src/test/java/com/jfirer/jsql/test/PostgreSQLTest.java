package com.jfirer.jsql.test;

import com.jfirer.jsql.SessionFactory;
import com.jfirer.jsql.SessionfactoryConfig;
import com.jfirer.jsql.annotation.AutoIncrement;
import com.jfirer.jsql.annotation.Pk;
import com.jfirer.jsql.annotation.Sql;
import com.jfirer.jsql.annotation.TableDef;
import com.jfirer.jsql.mapper.Mapper;
import com.jfirer.jsql.session.SqlSession;
import com.pgvector.PGvector;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.Accessors;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PostgreSQLTest
{
    private SessionFactory sessionFactory;

    @Before
    public void before()
    {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5433/test");
        dataSource.setUsername("root");
        dataSource.setPassword("xiksjk@!(s67");
        SessionfactoryConfig config = new SessionfactoryConfig();
        config.setDataSource(dataSource);
        sessionFactory = config.build();
    }

    @lombok.Data
    @Accessors(chain = true)
    @TableDef("test_id")
    public static class Data
    {
        @Pk
        @AutoIncrement
        private Integer id;
        private String  name;
        private float[] embedding;
    }

    @lombok.Data
    @Accessors(chain = true)
    @TableDef("test_id")
    public static class QueryResult
    {
        private String  name;
        private float[] embedding;
        private String   similarity;
    }

    @Mapper({Data.class, QueryResult.class})
    public interface QueryOp
    {
        @Sql(sql = "select name, 1-(embedding <=> ${input}) AS similarity  from Data order by embedding <=> ${input} limit 3 ", paramNames = "input")
        List<QueryResult> query(float[] input);

        @Sql(sql = "select name, ((embedding <=> ${input})) AS similarity  from Data order by similarity  ", paramNames = "input")
        List<QueryResult> query2(PGvector input);
    }

    @Test
    public void test()
    {
        /**
         * print(outputStr,'select name , 1-( embedding <=> ');
         * printParam(outputStr,sqlParams,input);
         * print(outputStr,' ) AS similarity from test_id order by ( embedding <=> ');
         * printParam(outputStr,sqlParams,input);
         * print(" ) limit 3");
         */
        List<String> list = List.of("黑头发，黑眼睛，就是中国人", "苹果草莓都好吃，但是我最爱吃西瓜", "天上的星星很亮", "2月14日是情人节");
        try (SqlSession session = sessionFactory.openSession())
        {
            QueryOp           mapper       = session.getMapper(QueryOp.class);
            String            queryContent = "黑头发，黑眼睛，就是中国人";

//            List<QueryResult> query        = mapper.query(SiliconflowAPI.sendEmbeddingRequest(queryContent));
            List<QueryResult> query = mapper.query2(new PGvector(SiliconflowAPI.sendEmbeddingRequest(queryContent)));
            System.out.println(query);
        }
    }
}
