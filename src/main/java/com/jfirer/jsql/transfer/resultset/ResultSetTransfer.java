package com.jfirer.jsql.transfer.resultset;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetTransfer
{
    ResultSetTransfer initialize(Class<?> type);

    Object transfer(ResultSet resultSet) throws SQLException;

}
