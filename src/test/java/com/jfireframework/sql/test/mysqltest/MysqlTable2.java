package com.jfireframework.sql.test.mysqltest;

import com.jfireframework.sql.annotation.Pk;
import com.jfireframework.sql.annotation.StandardColumnDef;
import com.jfireframework.sql.annotation.TableDef;
import com.jfireframework.sql.dbstructure.Constraint;
import com.jfireframework.sql.dbstructure.Constraint.Type;

@TableDef(name = "test_demo")
public class MysqlTable2
{
	@Pk
	@Constraint(type = Type.PRIMARY_KEY)
	@StandardColumnDef(isNullable = false)
	private String	id;
	private String	name;
}
