package com.jfireframework.sql.test.mysqltest;

import com.jfireframework.sql.annotation.Id;
import com.jfireframework.sql.annotation.TableEntity;

@TableEntity(name = "test_demo")
public class MysqlTable
{
	@Id
	private Integer	id;
	private String	name;
}
