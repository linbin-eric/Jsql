package com.jfireframework.sql.test.mysqltest;

import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;
import com.jfireframework.sql.idstrategy.AutoIncrement;

@TableEntity(name = "test_demo")
public class MysqlTable
{
    @Id
    @AutoIncrement
    private Integer id;
    private String  name;
    private int     age;
}
