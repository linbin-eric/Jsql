package com.jfirer.jsql.transfer;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetTransfer
{
    Object transfer(ResultSet resultSet) throws SQLException;

    ResultSetTransfer awareType(Class type);
}
