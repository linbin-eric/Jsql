package com.jfirer.jsql.dialect.impl;

import com.jfirer.jsql.dialect.Dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OracleDialect implements Dialect
{
    ThreeConsumer consumer;

    public OracleDialect(ThreeConsumer consumer)
    {
        this.consumer = consumer;
    }

    public OracleDialect()
    {
        this(ThreeConsumer::defaultAccept);
    }

    @Override
    public void fillStatement(PreparedStatement preparedStatement, List<Object> params) throws SQLException
    {
        int size = params.size();
        for (int i = 0; i < size; i++)
        {
            Object value = params.get(i);
            int    index = i + 1;
            if (value instanceof java.sql.Date)
            {
                preparedStatement.setDate(index, (java.sql.Date) value);
            }
            else if (value instanceof java.sql.Timestamp)
            {
                preparedStatement.setTimestamp(index, (java.sql.Timestamp) value);
            }
            else if (value instanceof java.util.Date)
            {
                Date date = (Date) value;
                preparedStatement.setTimestamp(index, new Timestamp(date.getTime()));
            }
            else if (value instanceof Calendar)
            {
                Calendar calendar = (Calendar) value;
                preparedStatement.setTimestamp(index, new Timestamp(calendar.getTimeInMillis()));
            }
            else
            {
                consumer.accept(preparedStatement, index, value);
            }
        }
    }
}
