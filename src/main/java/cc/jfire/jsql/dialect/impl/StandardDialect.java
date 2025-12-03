package cc.jfire.jsql.dialect.impl;

import cc.jfire.jsql.dialect.Dialect;
import cc.jfire.jsql.dialect.DialectDict;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StandardDialect implements Dialect
{
    ThreeConsumer consumer;
    private DialectDict product;

    public StandardDialect(ThreeConsumer consumer, DialectDict dialectDict)
    {
        this.consumer = consumer;
        this.product  = dialectDict;
    }

    public StandardDialect(DialectDict dialectDict)
    {
        this(ThreeConsumer::defaultAccept, dialectDict);
    }

    @Override
    public void fillStatement(PreparedStatement preparedStatement, List<Object> params) throws SQLException
    {
        int size = params.size();
        for (int i = 0; i < size; i++)
        {
            Object value = params.get(i);
            int    index = i + 1;
            if (consumer.accept(preparedStatement, index, value))
            {
                continue;
            }
            if (value instanceof java.sql.Date)
            {
                preparedStatement.setDate(index, (java.sql.Date) value);
            }
            else if (value instanceof java.sql.Timestamp)
            {
                preparedStatement.setTimestamp(index, (java.sql.Timestamp) value);
            }
            else if (value instanceof Date date)
            {
                preparedStatement.setTimestamp(index, new Timestamp(date.getTime()));
            }
            else if (value instanceof LocalDate localDate)
            {
                preparedStatement.setDate(index, java.sql.Date.valueOf(localDate));
            }
            else if (value instanceof LocalDateTime localDateTime)
            {
                preparedStatement.setTimestamp(index, Timestamp.valueOf(localDateTime));
            }
            else if (value instanceof Enum<?> enumValue)
            {
                preparedStatement.setString(index, enumValue.name());
            }
            else if (value instanceof Calendar calendar)
            {
                preparedStatement.setTimestamp(index, new Timestamp(calendar.getTimeInMillis()));
            }
            else
            {
                preparedStatement.setObject(index, value);
            }
        }
    }

    @Override
    public DialectDict product()
    {
        return product;
    }
}
