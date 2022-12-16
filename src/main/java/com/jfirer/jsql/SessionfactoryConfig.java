package com.jfirer.jsql;

import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.Verify;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jsql.dialect.Dialect;
import com.jfirer.jsql.dialect.impl.H2Dialect;
import com.jfirer.jsql.dialect.impl.MysqlDialect;
import com.jfirer.jsql.dialect.impl.OracleDialect;
import com.jfirer.jsql.executor.SqlExecutor;
import com.jfirer.jsql.executor.impl.FinalExecuteSqlExecutor;
import com.jfirer.jsql.executor.impl.OraclePageExecutor;
import com.jfirer.jsql.executor.impl.StandardPageExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SessionfactoryConfig
{
    private       DataSource        dataSource;
    private final List<SqlExecutor> sqlExecutors = new LinkedList<SqlExecutor>();
    private       Dialect           dialect;

    public SessionFactory build()
    {
        TRACEID.newTraceId();
        try
        {
            Verify.notNull(dataSource, "dataSource 对象不能为空");
            String productName = detectProductName();
            dialect = dialect == null ? generateDialect(productName) : dialect;
            return new SessionFactoryImpl(generateHeadSqlExecutor(productName), dataSource, dialect);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    private Dialect generateDialect(String productName)
    {
        if (productName.equals("mariadb") || "mysql".equals(productName))
        {
            return new MysqlDialect();
        }
        else if (productName.equals("oracle"))
        {
            return new OracleDialect();
        }
        else if (productName.equals("h2"))
        {
            return new H2Dialect();
        }
        else
        {
            throw new UnsupportedOperationException("不识别的数据库类型" + productName);
        }
    }

    private SqlExecutor generateHeadSqlExecutor(String productName)
    {
        if ("mysql".equalsIgnoreCase(productName) || "h2".equalsIgnoreCase(productName))
        {
            sqlExecutors.add(new StandardPageExecutor());
        }
        else if ("oracle".equalsIgnoreCase(productName))
        {
            sqlExecutors.add(new OraclePageExecutor());
        }
        sqlExecutors.add(new FinalExecuteSqlExecutor());
        Optional<SqlExecutor> minOrderExecutor = sqlExecutors.stream()//
                                                             .sorted((e1, e2) -> {
                                                                 int result = e2.order() - e1.order();
                                                                 if (result == 0)
                                                                 {
                                                                     throw new IllegalStateException(e1.getClass().getName() + "和" + e2.getClass().getName() + "的序号重复，这会导致不可预测的结果，请检查");
                                                                 }
                                                                 return result;
                                                             })//
                                                             .reduce((current, next) -> {
                                                                 next.setNext(current);
                                                                 return next;
                                                             });
        return minOrderExecutor.get();
    }

    private String detectProductName() throws SQLException
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            DatabaseMetaData md = connection.getMetaData();
            return md.getDatabaseProductName().toLowerCase();
        }
        finally
        {
            if (connection != null)
            {
                connection.close();
            }
        }
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public Dialect getDialect()
    {
        return dialect;
    }

    public void setDialect(Dialect dialect)
    {
        this.dialect = dialect;
    }

    public void addSqlExecutor(SqlExecutor sqlExecutor)
    {
        sqlExecutors.add(sqlExecutor);
    }
}
