package com.jfirer.jsql.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface Dialect
{
    /**
     * 填充参数到preparedStatement中
     *
     * @param preparedStatement
     * @param params
     * @throws SQLException
     */
    void fillStatement(PreparedStatement preparedStatement, List<Object> params) throws SQLException;

    @FunctionalInterface
    interface ThreeConsumer
    {
        void accept(PreparedStatement preparedStatement, int index, Object value) throws SQLException;

        static void defaultAccept(PreparedStatement preparedStatement, int index, Object value)
        {
            try
            {
                preparedStatement.setObject(index, value);
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
