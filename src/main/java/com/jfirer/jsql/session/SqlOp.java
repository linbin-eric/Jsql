package com.jfirer.jsql.session;

import com.jfirer.jsql.transfer.ResultSetTransfer;

import java.util.List;

public interface SqlOp
{
    int update(String sql, List<Object> params);

    /**
     * 插入一行数据，并且以String的形式返回自动生成的主键
     *
     * @param sql
     * @param params
     * @return
     */
    String insertReturnPk(String sql, List<Object> params);

    <T> T query(ResultSetTransfer transfer, String sql, List<Object> params);

    /**
     * 如果最后一个参数是Page，则会触发page查询
     *
     * @param transfer
     * @param sql
     * @param params
     * @return
     */
    <T> List<T> queryList(ResultSetTransfer transfer, String sql, List<Object> params);
}
